package com.payment_processing_system.strategies;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.CryptoNetworkEnum;
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
@DisplayName("CryptoPaymentStrategy")
public class CryptoPaymentStrategyTest {

    @Mock
    private PaymentValidationHelper validator;

    @InjectMocks
    private CryptoPaymentStrategy strategy;

    private static final String TRANSACTION_ID = "TXN-CRYPTO-123";

    /** Sets up validator mock to return valid crypto details. */
    private void mockValidCryptoDetails(String network) {
        when(validator.getSpecificKey(any(), anyString()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(1);
                    return switch (key) {
                        case "walletAddress" -> determineWalletAddress(network);
                        case "network" -> network;
                        default -> "validValue";
                    };
                });
        lenient().doNothing().when(validator).simulateRandomFailure(anyDouble(), anyString());
        lenient().when(validator.generateTransactionId()).thenReturn(TRANSACTION_ID);
    }

    private String determineWalletAddress(String network) {
        String walletAddress = null;
        if (CryptoNetworkEnum.BITCOIN.name().equals(network)) {
            walletAddress = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa";
        } else if (CryptoNetworkEnum.ETHEREUM.name().equals(network)) {
            walletAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEbb";
        }

        return walletAddress;
    }

    @Nested
    @DisplayName("process()")
    class ProcessTests {

        @Test
        @DisplayName("should successfully process valid Bitcoin payment")
        void process_validBitcoinRequest_returnsCompletedResponse() {
            // Valid Bitcoin address, network=BITCOIN, amount >= €10
            PaymentRequest request = PaymentTestHelper.createValidBitcoinRequest();

            mockValidCryptoDetails(CryptoNetworkEnum.BITCOIN.name());
            PaymentResponse response = strategy.process(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getTransactionId()).isEqualTo(TRANSACTION_ID);
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.CRYPTO);
            assertThat(response.getNetAmount()).isEqualByComparingTo(request.getAmount());
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should successfully process valid Ethereum payment")
        void process_validEthereumRequest_returnsCompletedResponse() {
            // Valid Ethereum address, network=ETHEREUM, amount >= €10
            PaymentRequest request = PaymentTestHelper.createValidBitcoinRequest();

            mockValidCryptoDetails(CryptoNetworkEnum.ETHEREUM.name());
            PaymentResponse response = strategy.process(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(response.getTransactionId()).isEqualTo(TRANSACTION_ID);
            assertThat(response.getMethod()).isEqualTo(PaymentMethodsEnum.CRYPTO);
            assertThat(response.getNetAmount()).isEqualByComparingTo(request.getAmount());
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should calculate fees correctly including gas fee")
        void process_validRequest_calculatesFeesWithGasFee() {
            // Fee should be 1% + gas fee (mocked)
            PaymentRequest request = PaymentTestHelper.createValidBitcoinRequest();

            // Create a spy to mock the gas fee calculation
            CryptoPaymentStrategy spyStrategy = spy(strategy);
            BigDecimal mockGasFee = new BigDecimal("2.50");
            doReturn(mockGasFee).when(spyStrategy).calculateNetworkGasFee();

            // Act
            mockValidCryptoDetails(CryptoNetworkEnum.BITCOIN.name()); // Also fixed to match request
            PaymentResponse response = spyStrategy.process(request);

            // Assert
            // Fee should be 1% of €100 = €1.00 + gas €2.50 = €3.50
            BigDecimal expectedFee = new BigDecimal("3.00");
            BigDecimal expectedGross = new BigDecimal("53.00");

            assertThat(response.getFee()).isEqualByComparingTo(expectedFee);
            assertThat(response.getGrossAmount()).isEqualByComparingTo(expectedGross);
            assertThat(response.getNetAmount()).isEqualByComparingTo(request.getAmount());
        }

        @Test
        @DisplayName("should throw exception when network congestion occurs")
        void process_highNetworkCongestion_throwsProcessingException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidBitcoinRequest();

            CryptoPaymentStrategy spyStrategy = spy(strategy);

            // Make checkNetworkCongestion always trigger the failure
            doAnswer(invocation -> {
                validator.simulateRandomFailure(0.3, "High network congestion - try again");
                return null;
            }).when(spyStrategy).checkNetworkCongestion();

            mockValidCryptoDetails(CryptoNetworkEnum.BITCOIN.name());

            // Configure validator to throw when congestion is triggered
            doThrow(new PaymentProcessingException("High network congestion - try again"))
                    .when(validator).simulateRandomFailure(eq(0.3), anyString());

            // Act & Assert
            assertThatThrownBy(() -> spyStrategy.process(request))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage("High network congestion - try again");
        }

        @Test
        @DisplayName("should throw exception for invalid wallet address format")
        void process_invalidWalletAddress_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createInvalidWalletRequest();

            when(validator.getSpecificKey(any(), eq("walletAddress"))).thenReturn("INVALID_WALLET_123");
            when(validator.getSpecificKey(any(), eq("network"))).thenReturn("BITCOIN");

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessage("Invalid Bitcoin address format");
        }

        @Test
        @DisplayName("should throw exception for unsupported network")
        void process_unsupportedNetwork_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createCryptoRequest(
                    "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                    "LITECOIN",
                    new BigDecimal("50.00")
            );

            when(validator.getSpecificKey(any(), eq("walletAddress"))).thenReturn("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
            when(validator.getSpecificKey(any(), eq("network"))).thenReturn("LITECOIN");

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessage("Network type is not supported");
        }

        @Test
        @DisplayName("should throw exception for amount below €10 minimum")
        void process_amountBelowMinimum_throwsValidationException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createBelowMinimumCryptoRequest();

            when(validator.getSpecificKey(any(), eq("walletAddress"))).thenReturn("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
            when(validator.getSpecificKey(any(), eq("network"))).thenReturn("BITCOIN");

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessage("Cryptocurrency payment minimum is €10.00");
        }

        @Test
        @DisplayName("should throw exception for missing wallet address")
        void process_missingWalletAddress_throwsException() {
            // Arrange
            PaymentRequest request = PaymentTestHelper.createValidBitcoinRequest();
            request.getPaymentDetails().remove("walletAddress");

            when(validator.getSpecificKey(any(), eq("walletAddress")))
                    .thenThrow(new PaymentValidationException("Missing required field: walletAddress"));

            // Act & Assert
            assertThatThrownBy(() -> strategy.process(request))
                    .isInstanceOf(PaymentValidationException.class)
                    .hasMessageContaining("walletAddress");
        }
    }
}
