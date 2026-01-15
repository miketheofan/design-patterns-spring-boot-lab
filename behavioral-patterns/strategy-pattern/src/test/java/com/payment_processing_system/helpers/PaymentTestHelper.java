package com.payment_processing_system.helpers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.enums.CurrenciesEnum;
import com.payment_processing_system.enums.PaymentMethodsEnum;

/**
 * Test helper for building payment test data.
 * Provides fluent builders for creating test objects.
 */
public class PaymentTestHelper {

    /** Creates a valid credit card payment request with default values */
    public static PaymentRequest createValidCreditCardRequest() {
        return createCreditCardRequest("4532015112830366", "123", "12/2026", "John Doe");
    }

    /** Creates a credit card payment request with custom details */
    public static PaymentRequest createCreditCardRequest(
        String cardNumber, String cvv, String expiryDate, String cardholderName) {
            PaymentRequest request = new PaymentRequest();
            request.setAmount(new BigDecimal("100.00"));
            request.setCurrency(CurrenciesEnum.EUR);
            request.setMethod(PaymentMethodsEnum.CREDIT_CARD);

            Map<String, Object> details = new HashMap<>();
            details.put("cardNumber", cardNumber);
            details.put("cvv", cvv);
            details.put("expiryDate", expiryDate);
            details.put("cardHolderName", cardholderName);

            request.setPaymentDetails(details);
            return request;
    }

    /** Creates a request with a specific amount. */
    public static PaymentRequest createRequestWithAmount(BigDecimal amount) {
        PaymentRequest request = createValidCreditCardRequest();
        request.setAmount(amount);
        return request;
    }

    /** Creates a request with an invalid (expired) card. */
    public static PaymentRequest createExpiredCardRequest() {
        return createCreditCardRequest("4532015112830366", "123", "01/2020", "John Doe");
    }

    /** Creates a request with an invalid CVV. */
    public static PaymentRequest createInvalidCvvRequest() {
        return createCreditCardRequest("4532015112830366", "12", "12/2026", "John Doe");
    }

    /** Creates a request with an invalid card number (fails Luhn check). */
    public static PaymentRequest createInvalidCardNumberRequest() {
        return createCreditCardRequest("1234567812345678", "123", "12/2026", "John Doe");
    }
}
