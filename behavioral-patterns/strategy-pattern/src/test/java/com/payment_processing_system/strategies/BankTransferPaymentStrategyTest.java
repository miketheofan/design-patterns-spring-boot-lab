package com.payment_processing_system.strategies;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.enums.TransactionStatus;
import com.payment_processing_system.exceptions.PaymentProcessingException;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("BankTransferPaymentStrategy")
public class BankTransferPaymentStrategyTest {

    @Mock
    private PaymentValidationHelper validator;

    @InjectMocks
    private BankTransferPaymentStrategy strategy;

    private static final String TRANSACTION_ID = "TXN-BANK-123";

    /** Sets up validator mock to return valid bank transfer details. */
    private void mockValidBankTransferDetails() {
        when(validator.getSpecificKey(any(), anyString()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(1);
                    return switch (key) {
                        case "iban" -> "GR1234567890";
                        case "bic_code" -> "12345";
                        case "card_holder_name" -> "John Doe";
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
        @DisplayName("should successfully process valid bank transfer payment")
        void process_validRequest_returnsCompletedResponse() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidBankTransferRequest();

            mockValidBankTransferDetails();

            // Act
            PaymentResponse response = strategy.process(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getTransactionId()).isEqualTo(TRANSACTION_ID);
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.BANK_TRANSFER);
            assertThat(response.getNetAmount()).isEqualByComparingTo(request.getAmount());
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should have zero fees for bank transfer")
        void process_validRequest_hasZeroFee() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createBankTransferRequest(
                    "GR9876543210", "54321", "Jane Smith", new BigDecimal("500.00"));

            mockValidBankTransferDetails();

            // Act
            PaymentResponse response = strategy.process(request);

            // Assert
            BigDecimal expectedFee = new BigDecimal("0.00");
            BigDecimal expectedGross = new BigDecimal("500.00");

            assertThat(response.getFee()).isEqualByComparingTo(expectedFee);
            assertThat(response.getGrossAmount()).isEqualByComparingTo(expectedGross);
            assertThat(response.getNetAmount()).isEqualByComparingTo(request.getAmount());
        }

        @Test
        @DisplayName("should throw exception for invalid IBAN format")
        void process_invalidIban_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createInvalidIbanRequest();

            when(validator.getSpecificKey(any(), eq("iban"))).thenReturn("INVALID123");
            when(validator.getSpecificKey(any(), eq("bic_code"))).thenReturn("12345");
            when(validator.getSpecificKey(any(), eq("card_holder_name"))).thenReturn("John Doe");

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessage("IBAN must be in format: GR[tokens]");
        }

        @Test
        @DisplayName("should throw exception for invalid BIC code format")
        void process_invalidBicCode_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createInvalidBicCodeRequest();

            when(validator.getSpecificKey(any(), eq("iban"))).thenReturn("GR1234567890");
            when(validator.getSpecificKey(any(), eq("bic_code"))).thenReturn("ABC");
            when(validator.getSpecificKey(any(), eq("card_holder_name"))).thenReturn("John Doe");

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessage("BIC Code is not supported");
        }

        @Test
        @DisplayName("should throw exception when insufficient funds")
        void process_insufficientFunds_throwsProcessingException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidBankTransferRequest();

            mockValidBankTransferDetails();
            doThrow(new PaymentProcessingException("Insufficient funds"))
                    .when(validator).simulateRandomFailure(eq(0.1), anyString());

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage("Insufficient funds");
        }

        @Test
        @DisplayName("should throw exception for missing IBAN")
        void process_missingIban_throwsException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidBankTransferRequest();
            request.getPaymentDetails().remove("iban");

            when(validator.getSpecificKey(any(), eq("iban")))
                    .thenThrow(new PaymentValidationException("Missing required field: iban"));

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessageContaining("iban");
        }

        @Test
        @DisplayName("should throw exception for missing BIC code")
        void process_missingBicCode_throwsException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidBankTransferRequest();
            request.getPaymentDetails().remove("bic_code");

            when(validator.getSpecificKey(any(), eq("iban"))).thenReturn("GR1234567890");
            when(validator.getSpecificKey(any(), eq("bic_code")))
                    .thenThrow(new PaymentValidationException("Missing required field: bic_code"));

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessageContaining("bic_code");
        }
    }
}
