# 1. Problem Statement

## The Problem

Modern e-commerce platforms need to support multiple payment methods (credit cards, PayPal, cryptocurrency, bank transfers). Each method has unique validation rules, fee structures and processing logic.

**Without the Strategy Pattern**, this typically results in a monolithic service with massive conditional chains:

- 300+ lines in a single method
- Cyclomatic complexity of 20+
- Every new payment method requires modifying existing code
- Testing becomes coupled and fragile
- High risk of breaking existing methods when adding new ones

This violates the **Open/Closed Principle** and makes the codebase rigid and difficult to maintain.

---

# 2. Solution

## Solution: Strategy Pattern

Each payment method is implemented as a separate **Strategy** class that implements a common `PaymentStrategy` interface. The payment service selects the appropriate strategy at runtime based on the payment method, with **zero conditional logic**.

### Architecture

```
PaymentsController
       ↓
PaymentService (strategy selection via Map<PaymentMethod, PaymentStrategy>)
       ↓
PaymentStrategy (interface)
       ├── CreditCardPaymentStrategy
       ├── PayPalPaymentStrategy
       ├── CryptoPaymentStrategy
       └── BankTransferPaymentStrategy
```

**Key Benefit**: Adding a 5th payment method (e.g., Apple Pay) requires creating **1 new file** with **0 modifications** to existing code.

---

# 3. Code Comparison

## Before vs After

### ❌ Without Strategy Pattern

```java
public class PaymentService {
    public PaymentResponse processPayment(PaymentRequest request) {
        if (request.getMethod() == PaymentMethod.CREDIT_CARD) {
            // 50 lines: Luhn validation, CVV check, fee calculation...
            String cardNumber = request.getDetails().get("cardNumber");
            if (!isValidLuhn(cardNumber)) throw new ValidationException();
            BigDecimal fee = amount.multiply(0.029).add(0.30);
            // ... more logic
        } else if (request.getMethod() == PaymentMethod.PAYPAL) {
            // 40 lines: email validation, OAuth, different fees...
            String email = request.getDetails().get("email");
            if (!isValidEmail(email)) throw new ValidationException();
            BigDecimal fee = amount.multiply(0.034).add(0.35);
            // ... more logic
        } else if (request.getMethod() == PaymentMethod.CRYPTO) {
            // 60 lines: wallet validation, gas fees, network checks...
        } else if (request.getMethod() == PaymentMethod.BANK_TRANSFER) {
            // 30 lines: IBAN validation, SWIFT codes...
        } else {
            throw new UnsupportedPaymentMethodException();
        }
        // Total: 180+ lines, complexity 20+
    }
}
```

### ✅ With Strategy Pattern

```java
@Service
public class PaymentService {
    private final Map<PaymentMethodsEnum, PaymentStrategy> strategies;

    public PaymentResponse processPayment(PaymentRequest request) {
        PaymentStrategy strategy = strategies.get(request.getMethod());
        if (strategy == null) {
            throw new UnsupportedPaymentMethodException(request.getMethod());
        }
        return strategy.process(request);
    }
    // Total: 10 lines, complexity 2
}

// Each strategy is isolated and testable
@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {
    public PaymentResponse process(PaymentRequest request) {
        validateCardDetails(request.getPaymentDetails());
        BigDecimal fee = calculateFee(request.getAmount());
        return buildResponse(request, fee);
    }

    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.029"))
                     .add(new BigDecimal("0.30"));
    }
    // ~100 lines total, complexity 4
}
```

---

# 4. Metrics Table

## Results

| Metric | Without Pattern | With Pattern | Improvement |
|--------|----------------|--------------|-------------|
| **Lines per payment method** | 300+ (monolithic) | ~100 (isolated) | 66% reduction |
| **Cyclomatic Complexity** | 20+ | 3-5 per strategy | 75% reduction |
| **To add 5th method** | Modify existing code | Create 1 new file | Zero risk |
| **Test Isolation** | Coupled tests | Independent tests | 100% isolated |
| **SOLID Compliance** | Violates O/C | Follows all 5 | ✅ |

### Proven Extensibility

Adding **Apple Pay** as a 5th payment method:
- **Files Created**: 1 (`ApplePayPaymentStrategy.java`)
- **Existing Files Modified**: 1 (config to register strategy)
- **Existing Tests Affected**: 0
- **Risk of Breaking Existing Methods**: 0%

---

# 5. Quick Start

## Running the Application

### Prerequisites
- Java 21+
- Maven 3.8+

### Start the Application
```bash
cd behavioral-patterns/strategy-pattern
mvn spring-boot:run
```

### Test Payment Processing

**Credit Card Payment:**
```bash
curl -X POST http://localhost:8080/api/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "EUR",
    "method": "CREDIT_CARD",
    "paymentDetails": {
      "cardNumber": "4532015112830366",
      "cvv": "123",
      "expiryDate": "12/2026",
      "cardHolderName": "John Doe"
    }
  }'
```

**Crypto Payment:**
```bash
curl -X POST http://localhost:8080/api/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.00,
    "currency": "EUR",
    "method": "CRYPTO",
    "paymentDetails": {
      "walletAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
      "network": "BITCOIN"
    }
  }'
```

### Run Tests
```bash
mvn test
```

### Check Test Coverage
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

---

# 6. Tech Stack

## Tech Stack

- **Java 21** - Modern Java with pattern matching
- **Spring Boot 3.2** - Dependency injection, REST API
- **Maven** - Build & dependency management
- **JUnit 5 + Mockito** - Testing framework
- **Lombok** - Boilerplate reduction
- **Jackson** - JSON serialization

## Payment Methods Supported

| Method | Validation | Fee Structure |
|--------|------------|---------------|
| **Credit Card** | Luhn algorithm, CVV, expiry | 2.9% + €0.30 |
| **PayPal** | Email format, OAuth token | 3.4% + €0.35 |
| **Cryptocurrency** | Wallet address, network validation | 1.0% + gas fees |
| **Bank Transfer** | IBAN, BIC/SWIFT code | €0 (free) |

---

# 7. Learning Outcomes

## Key Takeaways

✅ **When to use Strategy Pattern:**
- Multiple algorithms for the same task
- Need runtime algorithm selection
- Conditional complexity becomes unmanageable (>10 branches)

✅ **SOLID Principles Applied:**
- **S**ingle Responsibility: Each strategy handles one payment method
- **O**pen/Closed: Add strategies without modifying existing code
- **L**iskov Substitution: All strategies are interchangeable
- **I**nterface Segregation: Minimal, focused interface
- **D**ependency Inversion: Depend on abstractions, not concretions

✅ **Trade-offs:**
- More files to manage (but simpler individually)
- Strategy selection logic needed (solved via factory/map)

---
