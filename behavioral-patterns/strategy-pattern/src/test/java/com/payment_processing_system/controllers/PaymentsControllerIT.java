package com.payment_processing_system.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
    @DisplayName("POST /api/payments/process")
    class ProcessPaymentTests {

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

        @Test
        @DisplayName("should return 400 for invalid card number")
        void processPayment_invalidCardNumber_returnsBadRequest() throws Exception {
            String requestJson = """
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
                """;

            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("should return 400 for expired card")
        void processPayment_expiredCard_returnsBadRequest() throws Exception {
            String requestJson = """
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
                """;

            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("should return 400 for missing required fields")
        void processPayment_missingFields_returnsBadRequest() throws Exception {
            String requestJson = """
                {
                    "currency": "EUR",
                    "method": "CREDIT_CARD"
                }
                """;

            mockMvc.perform(post(PAYMENTS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isBadRequest());
        }
    }
}
