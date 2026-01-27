package com.payment_processing_system.strategies;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.enums.TransactionStatus;
import com.payment_processing_system.exceptions.PaymentValidationException;
import com.payment_processing_system.helpers.PaymentTestHelper;
import com.payment_processing_system.utils.PaymentValidationHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayPalPaymentStrategy")
public class PayPalPaymentStrategyTest {

    @Mock
    private PaymentValidationHelper validator;

    @InjectMocks
    private PayPalPaymentStrategy strategy;

    private static final String TRANSACTION_ID = "TXN-PAYPAL-123";

    /** Sets up validator mock to return valid PayPal details. */
    private void mockValidPayPalDetails() {
        when(validator.getSpecificKey(any(), anyString()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(1);
                    return switch (key) {
                        case "email" -> "test@gmail.com";
                        case "token" -> "Bearer 1234567890";
                        default -> "validValue";
                    };
                });
        lenient().doNothing().when(validator).simulateRandomFailure(anyDouble(), anyString());
        lenient().when(validator.generateTransactionId()).thenReturn(TRANSACTION_ID);
    }

    @Nested
    @DisplayName("process()")
    class ProcessTests {

        @Test
        @DisplayName("should successfully process valid PayPal payment")
        void process_validRequest_returnsCompletedResponse() {
            PaymentRequest request = PaymentTestHelper.createValidPayPalRequest();

            // Mock
            mockValidPayPalDetails();
            // Act
            PaymentResponse response = strategy.process(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getTransactionId()).isEqualTo(TRANSACTION_ID);
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.PAYPAL);
            assertThat(response.getNetAmount()).isEqualByComparingTo(request.getAmount());
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should calculate fees correctly")
        void process_validRequest_calculatesFeesCorrectly() {
            PaymentRequest request = PaymentTestHelper.createPayPalRequest(
                    "test@gmail.com", "Bearer test123456", new BigDecimal("100.00"));

            // Mock
            mockValidPayPalDetails();
            // Act
            PaymentResponse response = strategy.process(request);

            // Assert
            BigDecimal expectedFee = new BigDecimal("3.75");
            BigDecimal expectedGross = new BigDecimal("103.75");

            assertThat(response.getFee()).isEqualByComparingTo(expectedFee);
            assertThat(response.getGrossAmount()).isEqualByComparingTo(expectedGross);
            assertThat(response.getNetAmount()).isEqualByComparingTo(request.getAmount());
        }

        @Test
        @DisplayName("should throw exception for invalid email format")
        void process_invalidEmail_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createInvalidEmailRequest();

            when(validator.getSpecificKey(any(), eq("email"))).thenReturn("invalid-email");
            when(validator.getSpecificKey(any(), eq("token"))).thenReturn("Bearer abc123def4");

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessage("Email must be in format: smth@gmail.com");
        }

        @Test
        @DisplayName("should throw exception for invalid token format")
        void process_invalidToken_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createInvalidTokenRequest();

            when(validator.getSpecificKey(any(), eq("email"))).thenReturn("smth@gmail.com");
            when(validator.getSpecificKey(any(), eq("token"))).thenReturn("invalid-token");

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessage("Token must be in format: Bearer [10 tokens]");
        }

        @Test
        @DisplayName("should throw exception for missing email")
        void process_missingEmail_throwsException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidPayPalRequest();
            request.getPaymentDetails().remove("email");

            when(validator.getSpecificKey(any(), eq("email")))
                    .thenThrow(new PaymentValidationException("Missing required field: email"));

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessageContaining("email");

        }

        @Test
        @DisplayName("should throw exception for missing token")
        void process_missingToken_throwsException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidPayPalRequest();
            request.getPaymentDetails().remove("token");

            when(validator.getSpecificKey(any(), eq("email"))).thenReturn("user@gmail.com");
            when(validator.getSpecificKey(any(), eq("token")))
                    .thenThrow(new PaymentValidationException("Missing required field: token"));

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessageContaining("token");

        }
    }
}
