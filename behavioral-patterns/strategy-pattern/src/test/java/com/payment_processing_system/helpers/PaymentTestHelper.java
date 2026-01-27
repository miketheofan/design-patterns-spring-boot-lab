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
            PaymentRequest request = PaymentRequest.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency(CurrenciesEnum.EUR)
                    .method(PaymentMethodsEnum.CREDIT_CARD)
                    .build();

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

    // Bitcoin payments
    public static PaymentRequest createValidBitcoinRequest() {
        return createCryptoRequest(
                "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                "BITCOIN",
                new BigDecimal("50.00")
        );
    }

    // Ethereum payments
    public static PaymentRequest createValidEthereumRequest() {
        return createCryptoRequest(
                "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
                "ETHEREUM",
                new BigDecimal("100.00")
        );
    }

    // Generic crypto request
    public static PaymentRequest createCryptoRequest(
            String walletAddress,
            String network,
            BigDecimal amount
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("walletAddress", walletAddress);
        details.put("network", network);

        return PaymentRequest.builder()
                .amount(amount)
                .currency(CurrenciesEnum.EUR)
                .method(PaymentMethodsEnum.CRYPTO)
                .paymentDetails(details)
                .build();
    }

    // Below minimum amount
    public static PaymentRequest createBelowMinimumCryptoRequest() {
        return createCryptoRequest(
                "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                "BITCOIN",
                new BigDecimal("9.99")
        );
    }

    // Invalid wallet address
    public static PaymentRequest createInvalidWalletRequest() {
        return createCryptoRequest(
                "INVALID_WALLET_123",
                "BITCOIN",
                new BigDecimal("50.00")
        );
    }

    // PayPal payments
    public static PaymentRequest createValidPayPalRequest() {
        return createPayPalRequest(
                "user@gmail.com",
                "Bearer abc123def4",
                new BigDecimal("100.00")
        );
    }

    // Generic PayPal request
    public static PaymentRequest createPayPalRequest(
            String email,
            String token,
            BigDecimal amount
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("email", email);
        details.put("token", token);

        return PaymentRequest.builder()
                .amount(amount)
                .currency(CurrenciesEnum.EUR)
                .method(PaymentMethodsEnum.PAYPAL)
                .paymentDetails(details)
                .build();
    }

    // Invalid email format
    public static PaymentRequest createInvalidEmailRequest() {
        return createPayPalRequest(
                "invalid-email",
                "Bearer abc123def4",
                new BigDecimal("100.00")
        );
    }

    // Invalid token format
    public static PaymentRequest createInvalidTokenRequest() {
        return createPayPalRequest(
                "user@gmail.com",
                "InvalidToken",
                new BigDecimal("100.00")
        );
    }

    // Bank Transfer payments
    public static PaymentRequest createValidBankTransferRequest() {
        return createBankTransferRequest(
                "GR1234567890",
                "12345",
                "John Doe",
                new BigDecimal("500.00")
        );
    }

    // Generic Bank Transfer request
    public static PaymentRequest createBankTransferRequest(
            String iban,
            String bicCode,
            String cardHolderName,
            BigDecimal amount
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("iban", iban);
        details.put("bic_code", bicCode);
        details.put("card_holder_name", cardHolderName);

        return PaymentRequest.builder()
                .amount(amount)
                .currency(CurrenciesEnum.EUR)
                .method(PaymentMethodsEnum.BANK_TRANSFER)
                .paymentDetails(details)
                .build();
    }

    // Invalid IBAN format
    public static PaymentRequest createInvalidIbanRequest() {
        return createBankTransferRequest(
                "INVALID123",
                "12345",
                "John Doe",
                new BigDecimal("500.00")
        );
    }

    // Invalid BIC code format
    public static PaymentRequest createInvalidBicCodeRequest() {
        return createBankTransferRequest(
                "GR1234567890",
                "ABC",
                "John Doe",
                new BigDecimal("500.00")
        );
    }
}
