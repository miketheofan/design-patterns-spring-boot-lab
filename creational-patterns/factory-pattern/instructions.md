# DPSB-C001: Factory Pattern - Notification Service

## 🎯 Project Overview

Build a **multi-channel notification service** that demonstrates the Factory Pattern by creating different notification handlers based on runtime channel selection (Email, SMS, Push Notification, Slack).

### Business Scenario
A customer engagement platform needs to send notifications through multiple channels. Each channel has:
- Different configuration requirements
- Unique delivery mechanisms
- Channel-specific validation rules
- Varying rate limits and retry policies
- Different cost structures

### Why Factory Pattern?

**The Pain Without Factory:**
```java
// ❌ BAD: Client code tightly coupled to concrete classes
public void sendNotification(String channel, NotificationRequest request) {
    if (channel.equals("EMAIL")) {
        EmailNotification email = new EmailNotification(smtpHost, smtpPort, apiKey);
        email.send(request);
    } else if (channel.equals("SMS")) {
        SmsNotification sms = new SmsNotification(twilioSid, twilioToken, fromNumber);
        sms.send(request);
    } else if (channel.equals("PUSH")) {
        PushNotification push = new PushNotification(fcmServerKey, apnsKey);
        push.send(request);
    }
    // Adding new channel = modifying this method (violates Open/Closed)
}
```

**The Solution With Factory:**
```java
// ✅ GOOD: Factory creates the right handler, client doesn't care
NotificationHandler handler = notificationFactory.createHandler(channel);
handler.send(request);
// Adding new channel = add new implementation, zero changes to client code
```

---

## 🏗️ Architecture

```
NotificationController → NotificationService → NotificationFactory
                                                       ├── creates
                                                       ↓
                                              NotificationHandler (interface)
                                                       ├── EmailNotificationHandler
                                                       ├── SmsNotificationHandler
                                                       ├── PushNotificationHandler
                                                       └── SlackNotificationHandler
```

---

## 📋 Implementation Requirements

### Phase 1: Core Domain Models

**NotificationRequest**
```java
- String recipient (email/phone/deviceToken/slackChannel)
- String subject
- String message
- NotificationChannel channel (enum)
- Map<String, String> metadata
- Priority priority (LOW, NORMAL, HIGH, URGENT)
```

**NotificationChannel Enum**
```java
EMAIL, SMS, PUSH, SLACK
```

**NotificationResult**
```java
- String notificationId
- NotificationStatus status (SENT, FAILED, QUEUED)
- String channelUsed
- LocalDateTime sentAt
- String providerReference (external ID from provider)
- BigDecimal cost
- String errorMessage (if failed)
```

**Priority Enum**
```java
LOW, NORMAL, HIGH, URGENT
```

**NotificationStatus Enum**
```java
SENT, FAILED, QUEUED
```

---

### Phase 2: Factory Pattern Components

**1. NotificationHandler Interface**
```java
public interface NotificationHandler {
    /** Validates the notification request for this channel. */
    ValidationResult validate(NotificationRequest request);

    /** Sends the notification through this channel. */
    NotificationResult send(NotificationRequest request);

    /** Calculates the cost for sending this notification. */
    BigDecimal calculateCost(NotificationRequest request);

    /** Returns the channel this handler supports. */
    NotificationChannel getSupportedChannel();
}
```

**2. NotificationFactory**
```java
@Component
public class NotificationFactory {

    // Injected via constructor - all handlers registered in Spring context
    private final Map<NotificationChannel, NotificationHandler> handlers;

    public NotificationHandler createHandler(NotificationChannel channel) {
        NotificationHandler handler = handlers.get(channel);
        if (handler == null) {
            throw new UnsupportedChannelException("Channel not supported: " + channel);
        }
        return handler;
    }
}
```

**3. Factory Configuration** (like your PaymentStrategyConfig)
```java
@Configuration
public class NotificationFactoryConfig {

    @Bean
    public Map<NotificationChannel, NotificationHandler> notificationHandlers(
        EmailNotificationHandler emailHandler,
        SmsNotificationHandler smsHandler,
        PushNotificationHandler pushHandler,
        SlackNotificationHandler slackHandler
    ) {
        return Map.of(
            NotificationChannel.EMAIL, emailHandler,
            NotificationChannel.SMS, smsHandler,
            NotificationChannel.PUSH, pushHandler,
            NotificationChannel.SLACK, slackHandler
        );
    }
}
```

---

### Phase 3: Concrete Handlers

#### 1. EmailNotificationHandler (DPSB-C001-01)
**Validation:**
- Email format validation (RFC 5322 - basic pattern: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$`)
- Subject length (max 200 chars)
- Message length (max 10,000 chars)
- Recipient not empty

**Processing:**
- Simulate SMTP connection
- Generate unique message ID (UUID)
- Include metadata as email headers
- Simulate delivery confirmation (random success/failure)

**Cost:** €0.001 per email

**Example Details Map:**
```json
{
  "smtpHost": "smtp.example.com",
  "smtpPort": "587",
  "from": "noreply@example.com"
}
```

---

#### 2. SmsNotificationHandler (DPSB-C001-02)
**Validation:**
- Phone number format (E.164: `^\\+[1-9]\\d{1,14}$`)
- Message length (max 160 chars for single SMS, 1600 for multi-part)
- Recipient not empty

**Processing:**
- Simulate Twilio/AWS SNS API call
- Calculate segments (160 chars per segment)
- Generate SMS ID (UUID)
- Simulate delivery receipt (random success/failure)

**Cost Calculation:**
- €0.05 per SMS segment
- Multi-segment: message length / 160 (rounded up)

**Example Details Map:**
```json
{
  "provider": "Twilio",
  "fromNumber": "+1234567890"
}
```

---

#### 3. PushNotificationHandler (DPSB-C001-03)
**Validation:**
- Device token format (64 hex chars for FCM: `^[a-fA-F0-9]{64}$`)
- Payload size estimation (message + subject + metadata < 4KB)
- Recipient not empty

**Processing:**
- Simulate FCM/APNS API call
- Include notification icon, title, body
- Generate notification ID (UUID)
- Simulate delivery acknowledgment (random success/failure)

**Cost:** €0.0001 per push notification

**Example Details Map:**
```json
{
  "provider": "FCM",
  "icon": "notification_icon",
  "sound": "default"
}
```

---

#### 4. SlackNotificationHandler (DPSB-C001-04)
**Validation:**
- Channel name format (starts with # or @: `^[#@][a-zA-Z0-9_-]+$`)
- Message length (max 4000 chars)
- Recipient not empty

**Processing:**
- Simulate Slack Webhook API
- Format message with markdown support
- Include metadata as attachment fields
- Generate message timestamp

**Cost:** €0 (free - self-hosted webhook)

**Example Details Map:**
```json
{
  "webhookUrl": "https://hooks.slack.com/services/...",
  "botName": "NotificationBot",
  "iconEmoji": ":bell:"
}
```

---

### Phase 4: Service Layer

**NotificationService**
```java
@Service
public class NotificationService {

    private final NotificationFactory factory;

    public NotificationResult sendNotification(NotificationRequest request) {
        // 1. Get the appropriate handler from factory
        NotificationHandler handler = factory.createHandler(request.getChannel());

        // 2. Validate
        ValidationResult validation = handler.validate(request);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }

        // 3. Send
        NotificationResult result = handler.send(request);

        // 4. Log/audit (optional - can be added later)

        return result;
    }

    public List<NotificationChannel> getSupportedChannels() {
        return Arrays.asList(NotificationChannel.values());
    }

    public BigDecimal estimateCost(NotificationRequest request) {
        NotificationHandler handler = factory.createHandler(request.getChannel());
        return handler.calculateCost(request);
    }
}
```

---

### Phase 5: REST API

**NotificationController**
```java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // Constructor injection

    /** Sends a notification through the specified channel. */
    @PostMapping("/send")
    public ResponseEntity<NotificationResult> sendNotification(
        @Valid @RequestBody NotificationRequest request
    ) {
        NotificationResult result = notificationService.sendNotification(request);
        return ResponseEntity.ok(result);
    }

    /** Returns all supported notification channels. */
    @GetMapping("/channels")
    public ResponseEntity<List<String>> getSupportedChannels() {
        List<String> channels = notificationService.getSupportedChannels()
            .stream()
            .map(Enum::name)
            .toList();
        return ResponseEntity.ok(channels);
    }

    /** Estimates the cost of sending a notification. */
    @PostMapping("/cost-estimate")
    public ResponseEntity<BigDecimal> estimateCost(
        @RequestBody NotificationRequest request
    ) {
        BigDecimal cost = notificationService.estimateCost(request);
        return ResponseEntity.ok(cost);
    }
}
```

**Exception Handling**
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnsupportedChannelException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedChannel(UnsupportedChannelException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

---

### Phase 6: Testing Requirements

#### Unit Tests (80%+ coverage)

**EmailNotificationHandlerTest**
- ✅ Valid email request → success
- ✅ Invalid email format → validation failure
- ✅ Subject too long (>200 chars) → validation failure
- ✅ Message too long (>10,000 chars) → validation failure
- ✅ Empty recipient → validation failure
- ✅ Cost calculation = €0.001
- ✅ getSupportedChannel() returns EMAIL

**SmsNotificationHandlerTest**
- ✅ Valid phone request → success
- ✅ Invalid phone format → validation failure
- ✅ Message >160 chars → multi-segment cost (€0.10 for 2 segments)
- ✅ Message >1600 chars → validation failure
- ✅ Empty recipient → validation failure
- ✅ Single segment cost = €0.05
- ✅ getSupportedChannel() returns SMS

**PushNotificationHandlerTest**
- ✅ Valid device token → success
- ✅ Invalid token format → validation failure
- ✅ Payload too large (>4KB estimate) → validation failure
- ✅ Empty recipient → validation failure
- ✅ Cost calculation = €0.0001
- ✅ getSupportedChannel() returns PUSH

**SlackNotificationHandlerTest**
- ✅ Valid channel (#general) → success
- ✅ Valid user (@john) → success
- ✅ Invalid channel format → validation failure
- ✅ Message too long (>4000 chars) → validation failure
- ✅ Empty recipient → validation failure
- ✅ Cost calculation = €0
- ✅ getSupportedChannel() returns SLACK

**NotificationFactoryTest**
- ✅ createHandler(EMAIL) → returns EmailNotificationHandler instance
- ✅ createHandler(SMS) → returns SmsNotificationHandler instance
- ✅ createHandler(PUSH) → returns PushNotificationHandler instance
- ✅ createHandler(SLACK) → returns SlackNotificationHandler instance
- ✅ createHandler(null) → throws UnsupportedChannelException
- ✅ Factory with empty handlers map → throws exception

**NotificationServiceTest**
- ✅ sendNotification with EMAIL → uses EmailHandler
- ✅ sendNotification with SMS → uses SmsHandler
- ✅ sendNotification with invalid data → throws ValidationException
- ✅ getSupportedChannels() → returns all 4 channels
- ✅ estimateCost() → delegates to correct handler

#### Integration Tests

**NotificationControllerIT** (with @SpringBootTest + MockMvc)
- ✅ POST /send with valid EMAIL → 200 OK with result
- ✅ POST /send with valid SMS → 200 OK with result
- ✅ POST /send with valid PUSH → 200 OK with result
- ✅ POST /send with valid SLACK → 200 OK with result
- ✅ POST /send with invalid channel → 400 Bad Request
- ✅ POST /send with invalid email format → 400 Bad Request
- ✅ POST /send with missing required fields → 400 Bad Request
- ✅ GET /channels → 200 OK with array of 4 channels
- ✅ POST /cost-estimate with EMAIL → 200 OK with €0.001
- ✅ POST /cost-estimate with SMS (multi-segment) → 200 OK with correct cost

---

## 📊 Success Criteria

### Factory Pattern Demonstration
- ✅ Factory creates correct handler based on channel enum
- ✅ Client code (service/controller) never uses `new` for handlers
- ✅ Adding 5th channel (e.g., WhatsApp) requires:
  - New handler implementation
  - Registration in NotificationFactoryConfig
  - **ZERO changes to service/controller/factory logic**

### Code Quality
- ✅ All handlers implement NotificationHandler interface
- ✅ Constructor injection only (no @Autowired fields)
- ✅ Clean JavaDoc (one-line style: `/** Does X. */`)
- ✅ Proper use of Lombok (@Data, @Builder, @AllArgsConstructor, etc.)
- ✅ 80%+ test coverage
- ✅ All tests passing
- ✅ No code duplication (DRY principle)

### SOLID Principles Applied
- ✅ **Single Responsibility**: Each handler handles one channel
- ✅ **Open/Closed**: Add new channels without modifying existing code
- ✅ **Liskov Substitution**: All handlers can replace NotificationHandler
- ✅ **Interface Segregation**: NotificationHandler has focused methods
- ✅ **Dependency Inversion**: Service depends on interface, not concrete classes

### Documentation
- ✅ README.md with before/after comparison
- ✅ Complexity metrics (cyclomatic complexity with vs without factory)
- ✅ When to use Factory Pattern vs other patterns
- ✅ Trade-offs and alternatives
- ✅ Clear JavaDoc for all public methods

---

## 🧪 Sample API Calls

### Send Email Notification
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Welcome to our platform",
    "message": "Thank you for signing up! We are excited to have you.",
    "channel": "EMAIL",
    "priority": "NORMAL",
    "metadata": {
      "userId": "12345",
      "campaign": "welcome-series"
    }
  }'
```

**Expected Response:**
```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SENT",
  "channelUsed": "EMAIL",
  "sentAt": "2025-01-15T10:30:45",
  "providerReference": "msg_abc123",
  "cost": 0.001,
  "errorMessage": null
}
```

---

### Send SMS Notification
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "+306912345678",
    "subject": "",
    "message": "Your verification code is: 123456. Valid for 10 minutes.",
    "channel": "SMS",
    "priority": "HIGH",
    "metadata": {}
  }'
```

**Expected Response:**
```json
{
  "notificationId": "660e8400-e29b-41d4-a716-446655440001",
  "status": "SENT",
  "channelUsed": "SMS",
  "sentAt": "2025-01-15T10:31:22",
  "providerReference": "SM9abc123xyz",
  "cost": 0.05,
  "errorMessage": null
}
```

---

### Send Push Notification
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2",
    "subject": "New Message",
    "message": "You have a new message from John Doe",
    "channel": "PUSH",
    "priority": "NORMAL",
    "metadata": {
      "userId": "67890",
      "type": "chat_message"
    }
  }'
```

**Expected Response:**
```json
{
  "notificationId": "770e8400-e29b-41d4-a716-446655440002",
  "status": "SENT",
  "channelUsed": "PUSH",
  "sentAt": "2025-01-15T10:32:10",
  "providerReference": "fcm_xyz789",
  "cost": 0.0001,
  "errorMessage": null
}
```

---

### Send Slack Notification
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "#general",
    "subject": "Deployment Alert",
    "message": "Production deployment completed successfully. Version: v2.3.1",
    "channel": "SLACK",
    "priority": "NORMAL",
    "metadata": {
      "environment": "production",
      "version": "v2.3.1"
    }
  }'
```

**Expected Response:**
```json
{
  "notificationId": "880e8400-e29b-41d4-a716-446655440003",
  "status": "SENT",
  "channelUsed": "SLACK",
  "sentAt": "2025-01-15T10:33:05",
  "providerReference": "slack_1234567890.123456",
  "cost": 0.0,
  "errorMessage": null
}
```

---

### Get Supported Channels
```bash
curl http://localhost:8080/api/notifications/channels
```

**Expected Response:**
```json
["EMAIL", "SMS", "PUSH", "SLACK"]
```

---

### Estimate Cost
```bash
curl -X POST http://localhost:8080/api/notifications/cost-estimate \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "+306912345678",
    "message": "This is a test message that is longer than 160 characters so it will be split into multiple SMS segments and we can see how the cost calculation works for multi-segment messages.",
    "channel": "SMS"
  }'
```

**Expected Response:**
```json
0.10
```
*(2 segments × €0.05 = €0.10)*

---

## 🎯 Key Learning Objectives

### 1. Understand Factory Pattern vs Strategy Pattern
| Aspect | Factory Pattern | Strategy Pattern |
|--------|----------------|------------------|
| **Purpose** | Object creation | Algorithm selection |
| **Focus** | Encapsulate instantiation logic | Encapsulate behavior |
| **When to Use** | Complex object creation, multiple types | Runtime behavior swapping |
| **Client Knows** | Interface only | Interface + context |
| **Example** | Creating different handlers | Choosing payment methods |

### 2. Master Spring Bean Registration
- Map-based dependency injection
- Type-safe factory implementation
- Configuration class patterns
- Constructor injection best practices

### 3. Apply SOLID Principles
- **Open/Closed**: Add channels without modifying service
- **Dependency Inversion**: Depend on interfaces, not implementations
- **Single Responsibility**: Each handler does one thing

### 4. Test Factory-Based Code
- Mock the factory in service tests
- Test factory logic independently
- Integration tests verify correct wiring

---

## 📂 Project Structure

```
creational-patterns/factory-pattern/
├── src/
│   ├── main/java/com/notification_service/
│   │   ├── controllers/
│   │   │   └── NotificationController.java
│   │   ├── services/
│   │   │   └── NotificationService.java
│   │   ├── factories/
│   │   │   └── NotificationFactory.java
│   │   ├── handlers/
│   │   │   ├── NotificationHandler.java (interface)
│   │   │   ├── EmailNotificationHandler.java
│   │   │   ├── SmsNotificationHandler.java
│   │   │   ├── PushNotificationHandler.java
│   │   │   └── SlackNotificationHandler.java
│   │   ├── models/
│   │   │   ├── NotificationRequest.java
│   │   │   ├── NotificationResult.java
│   │   │   ├── NotificationChannel.java (enum)
│   │   │   ├── NotificationStatus.java (enum)
│   │   │   ├── Priority.java (enum)
│   │   │   └── ValidationResult.java
│   │   ├── configs/
│   │   │   └── NotificationFactoryConfig.java
│   │   ├── exceptions/
│   │   │   ├── UnsupportedChannelException.java
│   │   │   ├── ValidationException.java
│   │   │   ├── ErrorResponse.java
│   │   │   └── GlobalExceptionHandler.java
│   │   └── FactoryPatternApplication.java
│   └── test/java/com/notification_service/
│       ├── handlers/
│       │   ├── EmailNotificationHandlerTest.java
│       │   ├── SmsNotificationHandlerTest.java
│       │   ├── PushNotificationHandlerTest.java
│       │   └── SlackNotificationHandlerTest.java
│       ├── factories/
│       │   └── NotificationFactoryTest.java
│       ├── services/
│       │   └── NotificationServiceTest.java
│       ├── helpers/
│       │   └── NotificationTestHelper.java (optional)
│       └── integration/
│           └── NotificationControllerIT.java
├── instructions.md (this file)
├── README.md (to be created at the end)
├── requirements.md (optional - can document requirements)
└── pom.xml
```

---

## 🚀 Implementation Roadmap

### Week 1: Foundation
1. **Day 1-2**: Domain models + enums
   - NotificationRequest
   - NotificationResult
   - All enums (Channel, Status, Priority)
   - ValidationResult

2. **Day 3**: Factory components
   - NotificationHandler interface
   - NotificationFactory
   - NotificationFactoryConfig
   - Exceptions

### Week 2: Handlers (one per day)
3. **Day 4**: EmailNotificationHandler + tests
4. **Day 5**: SmsNotificationHandler + tests
5. **Day 6**: PushNotificationHandler + tests
6. **Day 7**: SlackNotificationHandler + tests

### Week 3: Integration
7. **Day 8**: Service layer + tests
8. **Day 9**: Controller + exception handling
9. **Day 10**: Integration tests
10. **Day 11**: README + documentation

---

## 💡 Bonus Challenges (Optional - After Core Complete)

### Level 1: Basic Enhancements
- [ ] Add async notification sending (CompletableFuture)
- [ ] Implement retry logic for failed notifications
- [ ] Add notification templates support

### Level 2: Advanced Features
- [ ] Rate limiting per channel (e.g., max 100 SMS/minute)
- [ ] Batch notifications (send to multiple recipients)
- [ ] Notification scheduling (send at specific time)

### Level 3: Production-Ready
- [ ] Add persistence (save notification history to database)
- [ ] Implement idempotency (prevent duplicate sends)
- [ ] Add monitoring/metrics (success rate, latency)
- [ ] Circuit breaker for external provider failures

---

## 📚 Reference Materials

### Factory Pattern Resources
- **Gang of Four**: Factory Method Pattern
- **Refactoring Guru**: https://refactoring.guru/design-patterns/factory-method
- **Spring Docs**: Bean Definition and Configuration

### Related Patterns
- **Abstract Factory**: When you need families of related objects
- **Builder**: When object construction is complex with many parameters
- **Prototype**: When you need to clone existing objects

### When NOT to Use Factory Pattern
- ❌ Only one concrete implementation exists
- ❌ Object creation is trivial (just `new` is fine)
- ❌ You need complex initialization logic (use Builder instead)
- ❌ Creating families of related objects (use Abstract Factory)

---

## ✅ Definition of Done

Before considering this project complete:

### Code Complete
- [ ] All 4 handlers implemented and working
- [ ] Factory correctly creates all handlers
- [ ] Service delegates to factory
- [ ] Controller exposes all 3 endpoints
- [ ] Exception handling implemented

### Tests Complete
- [ ] Unit tests for all 4 handlers (100% coverage)
- [ ] Factory unit tests (all scenarios)
- [ ] Service unit tests (with mocked factory)
- [ ] Integration tests (all endpoints, happy + sad paths)
- [ ] Overall test coverage ≥80%
- [ ] All tests passing

### Documentation Complete
- [ ] README.md with before/after examples
- [ ] Code complexity comparison (with vs without factory)
- [ ] All classes have JavaDoc
- [ ] API endpoints documented
- [ ] instructions.md updated with lessons learned

### Quality Gates
- [ ] No compiler warnings
- [ ] No code duplication
- [ ] SOLID principles applied
- [ ] Clean git history (conventional commits)
- [ ] Code review completed

---

**Ready to start? Begin with Phase 1 - Domain Models!**

Create the enums first (NotificationChannel, NotificationStatus, Priority), then build the request/result models. Good luck! 🚀
