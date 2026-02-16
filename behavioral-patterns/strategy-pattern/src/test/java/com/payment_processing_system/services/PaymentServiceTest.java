package com.payment_processing_system.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.payment_processing_system.strategies.BankTransferPaymentStrategy;
import com.payment_processing_system.strategies.CryptoPaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.enums.TransactionStatus;
import com.payment_processing_system.exceptions.UnsupportedPaymentMethodException;
import com.payment_processing_system.helpers.PaymentTestHelper;
import com.payment_processing_system.strategies.PaymentStrategy;


@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService")
public class PaymentServiceTest {

    @Mock
    private PaymentStrategy creditCardStrategy;
    @Mock
    private PaymentStrategy paypalStrategy;
    @Mock
    private CryptoPaymentStrategy cryptoStrategy;
    @Mock
    private BankTransferPaymentStrategy bankTransferStrategy;

    private Map<PaymentMethodsEnum, PaymentStrategy> strategyMap;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        strategyMap = new HashMap();
        strategyMap.put(PaymentMethodsEnum.CREDIT_CARD, creditCardStrategy);
        strategyMap.put(PaymentMethodsEnum.PAYPAL, paypalStrategy);
        strategyMap.put(PaymentMethodsEnum.CRYPTO, cryptoStrategy);
        strategyMap.put(PaymentMethodsEnum.BANK_TRANSFER, bankTransferStrategy);

        paymentService = new PaymentService(strategyMap);
    }

    @Nested
    @DisplayName("processPayment()")
    class ProcessPaymentTests {

        @Test
        @DisplayName("should successfully process payment using credit card strategy")
        void processPayment_creditCardRequest_callsCreditCardStrategy() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidCreditCardRequest();
            PaymentResponse expectedResponse = PaymentResponse.builder()
                    .status(TransactionStatus.COMPLETED)
                    .transactionId("TXN-123")
                    .method(PaymentMethodsEnum.CREDIT_CARD)
                    .netAmount(new BigDecimal("100.00"))
                    .fee(new BigDecimal("3.20"))
                    .grossAmount(new BigDecimal("103.20"))
                    .timestamp(LocalDateTime.now())
                    .build();

            when(creditCardStrategy.process(request)).thenReturn(expectedResponse);

            // Act
            PaymentResponse response = paymentService.processPayment(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getTransactionId()).isEqualTo("TXN-123");
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.CREDIT_CARD);
            verify(creditCardStrategy, times(1)).process(request);
            verifyNoInteractions(paypalStrategy);
        }

        @Test
        @DisplayName("should successfully process payment using PayPal strategy")
        void processPayment_paypalRequest_callsPaypalStrategy() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidCreditCardRequest();
            request.setMethod(PaymentMethodsEnum.PAYPAL);

            PaymentResponse expectedResponse = PaymentResponse.builder()
                    .status(TransactionStatus.COMPLETED)
                    .transactionId("TXN-456")
                    .method(PaymentMethodsEnum.PAYPAL)
                    .netAmount(new BigDecimal("100.00"))
                    .timestamp(LocalDateTime.now())
                    .build();

            when(paypalStrategy.process(request)).thenReturn(expectedResponse);

            // Act
            PaymentResponse response = paymentService.processPayment(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.PAYPAL);
            verify(paypalStrategy, times(1)).process(request);
            verifyNoInteractions(creditCardStrategy);
        }

        @Test
        @DisplayName("should successfully process payment using Crypto strategy")
        void processPayment_cryptoRequest_callsCryptoStrategy() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidCreditCardRequest();
            request.setMethod(PaymentMethodsEnum.CRYPTO);

            PaymentResponse expectedResponse = PaymentResponse.builder()
                    .status(TransactionStatus.COMPLETED)
                    .transactionId("TXN-456")
                    .method(PaymentMethodsEnum.CRYPTO)
                    .netAmount(new BigDecimal("100.00"))
                    .timestamp(LocalDateTime.now())
                    .build();

            when(cryptoStrategy.process(request)).thenReturn(expectedResponse);

            // Act
            PaymentResponse response = paymentService.processPayment(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.CRYPTO);
            verify(cryptoStrategy, times(1)).process(request);
            verifyNoInteractions(creditCardStrategy);
            verifyNoInteractions(bankTransferStrategy);
            verifyNoInteractions(paypalStrategy);
        }

        @Test
        @DisplayName("should successfully process payment using PayPal strategy")
        void processPayment_bankTransferRequest_callsBankTransferStrategy() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidCreditCardRequest();
            request.setMethod(PaymentMethodsEnum.BANK_TRANSFER);

            PaymentResponse expectedResponse = PaymentResponse.builder()
                    .status(TransactionStatus.COMPLETED)
                    .transactionId("TXN-456")
                    .method(PaymentMethodsEnum.BANK_TRANSFER)
                    .netAmount(new BigDecimal("100.00"))
                    .timestamp(LocalDateTime.now())
                    .build();

            when(bankTransferStrategy.process(request)).thenReturn(expectedResponse);

            // Act
            PaymentResponse response = paymentService.processPayment(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.BANK_TRANSFER);
            verify(bankTransferStrategy, times(1)).process(request);
            verifyNoInteractions(creditCardStrategy);
            verifyNoInteractions(paypalStrategy);
            verifyNoInteractions(cryptoStrategy);
        }

        @Test
        @DisplayName("should throw exception for unsupported payment method")
        void processPayment_unsupportedMethod_throwsException() {
            // Arrange - create a service with only 3 strategies (exclude CRYPTO)
            Map<PaymentMethodsEnum, PaymentStrategy> limitedStrategyMap = new HashMap<>();
            limitedStrategyMap.put(PaymentMethodsEnum.CREDIT_CARD, creditCardStrategy);
            limitedStrategyMap.put(PaymentMethodsEnum.PAYPAL, paypalStrategy);
            limitedStrategyMap.put(PaymentMethodsEnum.BANK_TRANSFER, bankTransferStrategy);

            PaymentService limitedService = new PaymentService(limitedStrategyMap);
            PaymentRequest request = PaymentTestHelper.createValidBitcoinRequest();

            // Act & Assert
            assertThatThrownBy(() -> limitedService.processPayment(request))
                    .isInstanceOf(UnsupportedPaymentMethodException.class)
                    .hasMessageContaining("Cryptocurrency")
                    .hasMessageContaining("not currently supported");

            verifyNoInteractions(creditCardStrategy, paypalStrategy, bankTransferStrategy);
        }

        @Test
        @DisplayName("should delegate to correct strategy based on payment method")
        void processPayment_differentMethods_delegatesToCorrectStrategy() {
            // Arrange
            PaymentRequest creditCardRequest = PaymentTestHelper.createValidCreditCardRequest();
            PaymentRequest paypalRequest = PaymentTestHelper.createValidCreditCardRequest();
            PaymentRequest cryptoRequest = PaymentTestHelper.createValidCreditCardRequest();
            PaymentRequest bankTransferRequest = PaymentTestHelper.createValidCreditCardRequest();
            paypalRequest.setMethod(PaymentMethodsEnum.PAYPAL);
            cryptoRequest.setMethod(PaymentMethodsEnum.CRYPTO);
            bankTransferRequest.setMethod(PaymentMethodsEnum.BANK_TRANSFER);

            PaymentResponse creditCardResponse = PaymentResponse.builder()
                    .status(TransactionStatus.COMPLETED)
                    .method(PaymentMethodsEnum.CREDIT_CARD)
                    .build();

            PaymentResponse paypalResponse = PaymentResponse.builder()
                    .status(TransactionStatus.COMPLETED)
                    .method(PaymentMethodsEnum.PAYPAL)
                    .build();

            PaymentResponse cryptoResponse = PaymentResponse.builder()
                    .status(TransactionStatus.COMPLETED)
                    .method(PaymentMethodsEnum.CRYPTO)
                    .build();

            PaymentResponse bankTransferResponse = PaymentResponse.builder()
                    .status(TransactionStatus.COMPLETED)
                    .method(PaymentMethodsEnum.BANK_TRANSFER)
                    .build();

            when(creditCardStrategy.process(creditCardRequest)).thenReturn(creditCardResponse);
            when(paypalStrategy.process(paypalRequest)).thenReturn(paypalResponse);
            when(cryptoStrategy.process(cryptoRequest)).thenReturn(cryptoResponse);
            when(bankTransferStrategy.process(bankTransferRequest)).thenReturn(bankTransferResponse);

            // Act
            PaymentResponse ccResponse = paymentService.processPayment(creditCardRequest);
            PaymentResponse ppResponse = paymentService.processPayment(paypalRequest);
            PaymentResponse crResponse = paymentService.processPayment(cryptoRequest);
            PaymentResponse btResponse = paymentService.processPayment(bankTransferRequest);

            // Assert
            assertThat(ccResponse.getMethod()).isEqualTo(PaymentMethodsEnum.CREDIT_CARD);
            assertThat(ppResponse.getMethod()).isEqualTo(PaymentMethodsEnum.PAYPAL);
            assertThat(crResponse.getMethod()).isEqualTo(PaymentMethodsEnum.CRYPTO);
            assertThat(btResponse.getMethod()).isEqualTo(PaymentMethodsEnum.BANK_TRANSFER);
            verify(creditCardStrategy, times(1)).process(creditCardRequest);
            verify(paypalStrategy, times(1)).process(paypalRequest);
            verify(cryptoStrategy, times(1)).process(cryptoRequest);
            verify(bankTransferStrategy, times(1)).process(bankTransferRequest);
}
    }
}
