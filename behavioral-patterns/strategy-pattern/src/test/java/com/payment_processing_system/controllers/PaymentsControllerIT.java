package com.payment_processing_system.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the PaymentsController.
 * Tests the full request/response flow with real Spring context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PaymentsController Integration Tests")
public class PaymentsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private static final String PAYMENTS_URL = "/api/payments/process";

    @Nested
    @DisplayName("Credit Card Payments")
    class CreditCardPaymentTests {

        @Test
        @DisplayName("should return 200 OK for valid credit card payment")
        void processPayment_validCreditCard_returnsOk() throws Exception {
            String requestJson = """
                {
                    "amount": 100.00,
                    "currency": "EUR",
                    "method": "CREDIT_CARD",
                    "paymentDetails": {
                        "cardNumber": "4532015112830366",
                        "cvv": "123",
                        "expiryDate": "12/2026",
                        "cardHolderName": "John Doe"
                    }
                }
                """;

            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.method").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.netAmount").value(100.00))
                .andExpect(jsonPath("$.fee").value(3.20))
                .andExpect(jsonPath("$.grossAmount").value(103.20))
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidCreditCardScenarios")
        @DisplayName("should return 400 for invalid credit card inputs")
        void processPayment_invalidCreditCard_returnsBadRequest(String scenario, String requestJson) throws Exception {
            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }

        private static Stream<Arguments> invalidCreditCardScenarios() {
            return Stream.of(
                Arguments.of(
                    "invalid card number",
                    """
                    {
                        "amount": 50.00,
                        "currency": "USD",
                        "method": "CREDIT_CARD",
                        "paymentDetails": {
                            "cardNumber": "1234567812345678",
                            "cvv": "456",
                            "expiryDate": "12/2026",
                            "cardHolderName": "Jane Smith"
                        }
                    }
                    """
                ),
                Arguments.of(
                    "expired card",
                    """
                    {
                        "amount": 75.00,
                        "currency": "GBP",
                        "method": "CREDIT_CARD",
                        "paymentDetails": {
                            "cardNumber": "4532015112830366",
                            "cvv": "789",
                            "expiryDate": "01/2020",
                            "cardHolderName": "Bob Johnson"
                        }
                    }
                    """
                ),
                Arguments.of(
                    "invalid CVV format",
                    """
                    {
                        "amount": 50.00,
                        "currency": "EUR",
                        "method": "CREDIT_CARD",
                        "paymentDetails": {
                            "cardNumber": "4532015112830366",
                            "cvv": "12",
                            "expiryDate": "12/2026",
                            "cardHolderName": "John Doe"
                        }
                    }
                    """
                )
            );
        }
    }

    @Nested
    @DisplayName("PayPal Payments")
    class PayPalPaymentTests {

        @Test
        @DisplayName("should return 200 OK for valid PayPal payment")
        void processPayment_validPayPal_returnsOk() throws Exception {
            String requestJson = """
                  {
                      "amount": 150.00,
                      "currency": "USD",
                      "method": "PAYPAL",
                      "paymentDetails": {
                          "email": "user@example.com",
                          "payerId": "PAYERID123456"
                      }
                  }
                  """;

            mockMvc.perform(post(PAYMENTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.method").value("PAYPAL"))
                    .andExpect(jsonPath("$.netAmount").value(150.00))
                    .andExpect(jsonPath("$.fee").exists())
                    .andExpect(jsonPath("$.grossAmount").exists())
                    .andExpect(jsonPath("$.transactionId").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPayPalScenarios")
        @DisplayName("should return 400 for invalid PayPal inputs")
        void processPayment_invalidPayPal_returnsBadRequest(String scenario, String requestJson) throws Exception {
            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }

        private static Stream<Arguments> invalidPayPalScenarios() {
            return Stream.of(
                Arguments.of(
                    "invalid email",
                    """
                    {
                        "amount": 75.00,
                        "currency": "EUR",
                        "method": "PAYPAL",
                        "paymentDetails": {
                            "email": "invalid-email",
                            "payerId": "PAYERID123456"
                        }
                    }
                    """
                ),
                Arguments.of(
                    "missing payer ID",
                    """
                    {
                        "amount": 100.00,
                        "currency": "USD",
                        "method": "PAYPAL",
                        "paymentDetails": {
                            "email": "user@example.com"
                        }
                    }
                    """
                )
            );
        }
    }

    @Nested
    @DisplayName("Cryptocurrency Payments")
    class CryptoPaymentTests {

        @Test
        @DisplayName("should return 200 OK for valid crypto payment")
        void processPayment_validCrypto_returnsOk() throws Exception {
            String requestJson = """
                  {
                      "amount": 0.05,
                      "currency": "BTC",
                      "method": "CRYPTO",
                      "paymentDetails": {
                          "walletAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                          "cryptoCurrency": "BTC",
                          "network": "MAINNET"
                      }
                  }
                  """;

            mockMvc.perform(post(PAYMENTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.method").value("CRYPTO"))
                    .andExpect(jsonPath("$.netAmount").value(0.05))
                    .andExpect(jsonPath("$.fee").exists())
                    .andExpect(jsonPath("$.grossAmount").exists())
                    .andExpect(jsonPath("$.transactionId").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidCryptoScenarios")
        @DisplayName("should return 400 for invalid crypto inputs")
        void processPayment_invalidCrypto_returnsBadRequest(String scenario, String requestJson) throws Exception {
            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }

        private static Stream<Arguments> invalidCryptoScenarios() {
            return Stream.of(
                Arguments.of(
                    "invalid wallet address",
                    """
                    {
                        "amount": 0.01,
                        "currency": "BTC",
                        "method": "CRYPTO",
                        "paymentDetails": {
                            "walletAddress": "invalid-wallet",
                            "cryptoCurrency": "BTC",
                            "network": "MAINNET"
                        }
                    }
                    """
                ),
                Arguments.of(
                    "unsupported cryptocurrency",
                    """
                    {
                        "amount": 100.00,
                        "currency": "DOGE",
                        "method": "CRYPTO",
                        "paymentDetails": {
                            "walletAddress": "DH5yaieqoZN36fDVciNyRueRGvGLR3mr7L",
                            "cryptoCurrency": "DOGE",
                            "network": "MAINNET"
                        }
                    }
                    """
                )
            );
        }
    }

    @Nested
    @DisplayName("Bank Transfer Payments")
    class BankTransferPaymentTests {

        @Test
        @DisplayName("should return 200 OK for valid bank transfer payment")
        void processPayment_validBankTransfer_returnsOk() throws Exception {
            String requestJson = """
                  {
                      "amount": 500.00,
                      "currency": "EUR",
                      "method": "BANK_TRANSFER",
                      "paymentDetails": {
                          "iban": "DE89370400440532013000",
                          "bic": "COBADEFFXXX",
                          "accountHolderName": "John Doe"
                      }
                  }
                  """;

            mockMvc.perform(post(PAYMENTS_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.method").value("BANK_TRANSFER"))
                    .andExpect(jsonPath("$.netAmount").value(500.00))
                    .andExpect(jsonPath("$.fee").exists())
                    .andExpect(jsonPath("$.grossAmount").exists())
                    .andExpect(jsonPath("$.transactionId").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidBankTransferScenarios")
        @DisplayName("should return 400 for invalid bank transfer inputs")
        void processPayment_invalidBankTransfer_returnsBadRequest(String scenario, String requestJson) throws Exception {
            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }

        private static Stream<Arguments> invalidBankTransferScenarios() {
            return Stream.of(
                Arguments.of(
                    "invalid IBAN",
                    """
                    {
                        "amount": 200.00,
                        "currency": "EUR",
                        "method": "BANK_TRANSFER",
                        "paymentDetails": {
                            "iban": "INVALID1234567890",
                            "bic": "COBADEFFXXX",
                            "accountHolderName": "Jane Smith"
                        }
                    }
                    """
                ),
                Arguments.of(
                    "invalid BIC/SWIFT code",
                    """
                    {
                        "amount": 300.00,
                        "currency": "USD",
                        "method": "BANK_TRANSFER",
                        "paymentDetails": {
                            "iban": "DE89370400440532013000",
                            "bic": "INVALID",
                            "accountHolderName": "Bob Johnson"
                        }
                    }
                    """
                )
            );
        }
    }

    @Nested
    @DisplayName("General Validation Tests")
    class GeneralValidationTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidGeneralScenarios")
        @DisplayName("should return 400 for invalid general inputs")
        void processPayment_invalidGeneralInputs_returnsBadRequest(String scenario, String requestJson) throws Exception {
            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }

        private static Stream<Arguments> invalidGeneralScenarios() {
            return Stream.of(
                Arguments.of(
                    "missing required fields",
                    """
                    {
                        "currency": "EUR",
                        "method": "CREDIT_CARD"
                    }
                    """
                ),
                Arguments.of(
                    "zero amount",
                    """
                    {
                        "amount": 0.00,
                        "currency": "EUR",
                        "method": "CREDIT_CARD",
                        "paymentDetails": {
                            "cardNumber": "4532015112830366",
                            "cvv": "123",
                            "expiryDate": "12/2026",
                            "cardHolderName": "John Doe"
                        }
                    }
                    """
                ),
                Arguments.of(
                    "negative amount",
                    """
                    {
                        "amount": -50.00,
                        "currency": "USD",
                        "method": "PAYPAL",
                        "paymentDetails": {
                            "email": "user@example.com",
                            "payerId": "PAYERID123456"
                        }
                    }
                    """
                ),
                Arguments.of(
                    "null payment method",
                    """
                    {
                        "amount": 100.00,
                        "currency": "EUR",
                        "paymentDetails": {
                            "cardNumber": "4532015112830366",
                            "cvv": "123",
                            "expiryDate": "12/2026",
                            "cardHolderName": "John Doe"
                        }
                    }
                    """
                )
            );
        }
    }
}
