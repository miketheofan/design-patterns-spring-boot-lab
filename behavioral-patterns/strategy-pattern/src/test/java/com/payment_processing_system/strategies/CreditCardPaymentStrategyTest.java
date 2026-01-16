package com.payment_processing_system.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.enums.TransactionStatus;
import com.payment_processing_system.exceptions.PaymentProcessingException;
import com.payment_processing_system.exceptions.PaymentValidationException;
import com.payment_processing_system.helpers.PaymentTestHelper;
import com.payment_processing_system.utils.PaymentValidationHelper;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditCardPaymentStrategy")
public class CreditCardPaymentStrategyTest {

    @Mock
    private PaymentValidationHelper validator;

    @InjectMocks
    private CreditCardPaymentStrategy strategy;

    /** Sets up validator mock to return valid credit card details. */
    private void mockValidCardDetails() {
        when(validator.getSpecificKey(any(), anyString()))
            .thenAnswer(invocation -> {
                String key = invocation.getArgument(1);
                return switch (key) {
                    case "cardNumber" -> "4532015112830366";
                    case "cvv" -> "123";
                    case "expiryDate" -> "12/2026";
                    case "cardHolderName" -> "John Doe";
                    default -> "validValue";
                };
            });
        doNothing().when(validator).validatePattern(anyString(), anyString(), anyString());
        lenient().doNothing().when(validator).simulateRandomFailure(anyDouble(), anyString());
    }

    @Nested
    @DisplayName("process()")
    class ProcessTests {

        @Test
        @DisplayName("should successfully process valid credit card payment")
        void process_validRequest_returnsCompletedReponse() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidCreditCardRequest();
            String expectedTransactionId = "TXN-123456";

            mockValidCardDetails();
            when(validator.generateTransactionId()).thenReturn(expectedTransactionId);

            // Act
            PaymentResponse response = strategy.process(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getTransactionId()).isEqualTo(expectedTransactionId);
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.CREDIT_CARD);
            assertThat(response.getNetAmount()).isEqualByComparingTo(request.getAmount());
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should calculate fees correctly")
        void process_validRequest_calculatesFeesCorrectly() {
            // Arrange
            BigDecimal amount = new BigDecimal("100.00");
            PaymentRequest request = PaymentTestHelper.createRequestWithAmount(amount);

            mockValidCardDetails();
            when(validator.generateTransactionId()).thenReturn("TXN-123");

            // Act
            PaymentResponse response = strategy.process(request);
            
            // Assert
            // Fee should be 2.9% + €0.30 = €3.20
            BigDecimal expectedFee = new BigDecimal("3.20");
            BigDecimal expectedGross = new BigDecimal("103.20");
            
            assertThat(response.getFee()).isEqualByComparingTo(expectedFee);
            assertThat(response.getGrossAmount()).isEqualByComparingTo(expectedGross);
        }

        @Test
        @DisplayName("should throw exception for invalid CVV")
        void process_invalidCvv_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createInvalidCvvRequest();
            
            when(validator.getSpecificKey(any(), anyString())).thenReturn("12"); // Invalid 2-digit CVV
            doThrow(new PaymentValidationException("CVV must be 3-4 digits"))
                .when(validator).validatePattern(eq("12"), anyString(), anyString());

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessage("CVV must be 3-4 digits");
        }

        @Test
        @DisplayName("should throw exception for expired card")
        void process_expiredCard_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createExpiredCardRequest();
            
            when(validator.getSpecificKey(any(), anyString()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(1);
                    if ("expiryDate".equals(key)) return "01/2020";
                    return "validValue";
                });
            doNothing().when(validator).validatePattern(anyString(), anyString(), anyString());

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                .isInstanceOf(PaymentValidationException.class)
                .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("should throw exception when payment processing fails")
        void process_processingFailure_throwsProcessingException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidCreditCardRequest();

            mockValidCardDetails();
            doThrow(new PaymentProcessingException("Insufficient funds"))
                .when(validator).simulateRandomFailure(anyDouble(), anyString());

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessage("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("calculateFee()")
    class CalculateFeeTests {

        @Test
        @DisplayName("should calculate fee for small amount")
        void calculateFee_smallAmount_returnsCorrectFee() {
            // Arrange
            BigDecimal amount = new BigDecimal("10.00");

            // Act
            BigDecimal fee = strategy.calculateFee(amount);

            // Assert
            // 2.9% of 10 = 0.29 + 0.30 = 0.59
            assertThat(fee).isEqualByComparingTo(new BigDecimal("0.59"));
        }

        @Test
        @DisplayName("should calculate fee for large amount")
        void calculateFee_largeAmount_returnsCorrectFee() {
            // Arrange
            BigDecimal amount = new BigDecimal("1000.00");

            // Act
            BigDecimal fee = strategy.calculateFee(amount);

            // Assert
            // 2.9% of 1000 = 29.00 + 0.30 = 29.30
            assertThat(fee).isEqualByComparingTo(new BigDecimal("29.30"));
        }

        @Test
        @DisplayName("should round fee to 2 decimal places")
        void calculateFee_fractionalResult_roundsToTwoDecimals() {
            // Arrange
            BigDecimal amount = new BigDecimal("33.33");

            // Act
            BigDecimal fee = strategy.calculateFee(amount);

            // Assert
            assertThat(fee.scale()).isEqualTo(2);
            assertThat(fee).isEqualByComparingTo(new BigDecimal("1.27")); // 0.97 + 0.30 rounded
        }
    }
}
