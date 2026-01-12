# Payment Processing System - Requirements

## Business Problem

Build a payment processing REST API that supports multiple payment methods. The system must be easily extensible to add new payment methods without modifying existing code.

## Supported Payment Methods

### 1. Credit Card
**Validation:**
- Card number must pass Luhn algorithm
- CVV must be 3-4 digits
- Expiry date must not be in the past
- Cardholder name required

**Fee:** 2.9% + €0.30 per transaction

**Edge Cases:**
- Insufficient funds (simulate)
- Expired card
- Invalid card number

---

### 2. PayPal
**Validation:**
- Valid email format
- OAuth token present (simulate validation)

**Fee:** 3.4% + €0.35 per transaction

**Edge Cases:**
- Invalid email
- Missing/invalid token
- Insufficient PayPal balance (simulate)

---

### 3. Cryptocurrency
**Validation:**
- Valid wallet address format (Bitcoin or Ethereum)
- Network specified (BITCOIN/ETHEREUM)
- Amount meets minimum threshold (€10)

**Fee:** 1.0% + network gas fees (simulate)

**Edge Cases:**
- Invalid wallet address
- Amount below minimum
- High network congestion (simulate)

---

### 4. Bank Transfer
**Validation:**
- Valid IBAN format with checksum
- Valid SWIFT/BIC code
- Account holder name required

**Fee:** Free (€0)

**Edge Cases:**
- Invalid IBAN checksum
- Missing SWIFT code
- Transfer limit exceeded (simulate €10,000 limit)

---

## API Endpoints

### POST /api/payments/process
Process a payment using specified payment method.

**Request:**
```json
{
  "amount": 100.00,
  "currency": "EUR",
  "method": "CREDIT_CARD",
  "paymentDetails": {
    "cardNumber": "4532015112830366",
    "cvv": "123",
    "expiryDate": "12/2025",
    "cardholderName": "John Doe"
  }
}
```

**Success Response (200):**
```json
{
  "success": true,
  "transactionId": "TXN-123456789",
  "amount": 100.00,
  "currency": "EUR",
  "fee": 3.20,
  "totalAmount": 103.20,
  "method": "CREDIT_CARD",
  "status": "COMPLETED",
  "timestamp": "2025-01-12T10:30:00Z"
}
```

**Validation Error Response (400):**
```json
{
  "success": false,
  "errors": [
    "Invalid card number - failed Luhn check",
    "CVV must be 3-4 digits"
  ]
}
```

**Processing Error Response (500):**
```json
{
  "success": false,
  "error": "Payment processing failed: Insufficient funds"
}
```

---

### GET /api/payments/methods
List all available payment methods with their fees.

**Response (200):**
```json
{
  "methods": [
    {
      "method": "CREDIT_CARD",
      "name": "Credit Card",
      "feePercentage": 2.9,
      "fixedFee": 0.30
    },
    {
      "method": "PAYPAL",
      "name": "PayPal",
      "feePercentage": 3.4,
      "fixedFee": 0.35
    },
    {
      "method": "CRYPTO",
      "name": "Cryptocurrency",
      "feePercentage": 1.0,
      "fixedFee": 0.0
    },
    {
      "method": "BANK_TRANSFER",
      "name": "Bank Transfer",
      "feePercentage": 0.0,
      "fixedFee": 0.0
    }
  ]
}
```

---

## Acceptance Criteria

### Functional
- [ ] All 4 payment methods work end-to-end
- [ ] Validation rules enforced for each method
- [ ] Fee calculation correct for each method
- [ ] Transaction ID generated for successful payments
- [ ] Proper error responses for validation failures
- [ ] Proper error responses for processing failures

### Code Quality
- [ ] Easy to add 5th payment method without modifying existing code
- [ ] No large if/else or switch statements for payment method selection
- [ ] Each payment method independently testable
- [ ] Clean separation of concerns
- [ ] SOLID principles applied

### Testing
- [ ] Unit tests for each payment method (validation & processing)
- [ ] Unit tests for fee calculations
- [ ] Integration tests for end-to-end payment flow
- [ ] Test coverage > 80%
- [ ] All edge cases covered

### Documentation
- [ ] README explains the solution approach
- [ ] Code comments explain complex logic (e.g., Luhn algorithm)
- [ ] API examples provided

---

## Success Metrics

**Extensibility Proof:**
Adding Apple Pay (5th payment method) should require:
- Creating new files: Yes
- Modifying existing files: No (except adding to factory/registry)
- Re-testing existing payment methods: No

**Code Quality Metrics:**
- Lines of code per payment method: < 100
- Cyclomatic complexity per method: < 5
- No code duplication across payment methods

---

## Notes

- All external integrations (bank APIs, PayPal, crypto networks) should be **simulated**
- Focus on demonstrating clean, extensible architecture
- Think about how you'd explain your design decisions in a code review
