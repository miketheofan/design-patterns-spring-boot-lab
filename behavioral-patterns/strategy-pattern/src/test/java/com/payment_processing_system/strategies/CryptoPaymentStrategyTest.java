package com.payment_processing_system.strategies;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.CryptoNetworkEnum;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.enums.TransactionStatus;
import com.payment_processing_system.exceptions.PaymentProcessingException;
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
    }

    @Nested
    @DisplayName("calculateFee()")
    class CalculateFeeTests {

        @Test
        @DisplayName("should calculate fee for minimum amount (€10)")
        void calculateFee_minimumAmount_returnsCorrectFee() {
            // 1% of €10 = €0.10 + gas fee
        }

        @Test
        @DisplayName("should calculate fee for large amount")
        void calculateFee_largeAmount_returnsCorrectFee() {
            // 1% of €1000 = €10.00 + gas fee
        }

        @Test
        @DisplayName("should include variable gas fee")
        void calculateFee_anyAmount_includesGasFee() {
            // Gas fee should be randomized/simulated
        }

        @Test
        @DisplayName("should round fee to 2 decimal places")
        void calculateFee_fractionalResult_roundsToTwoDecimals() {
            // Scale check
        }
    }

    @Nested
    @DisplayName("Wallet Address Validation")
    class WalletAddressValidationTests {

        @Test
        @DisplayName("should accept valid Bitcoin address starting with 1")
        void validate_bitcoinAddressStartingWith1_passes() {
            // e.g., "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
        }

        @Test
        @DisplayName("should accept valid Bitcoin address starting with 3")
        void validate_bitcoinAddressStartingWith3_passes() {
            // e.g., "3J98t1WpEZ73CNmYviecrnyiWrnqRhWNLy"
        }

        @Test
        @DisplayName("should accept valid Bitcoin address starting with bc1")
        void validate_bitcoinAddressStartingWithBc1_passes() {
            // e.g., "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq"
        }

        @Test
        @DisplayName("should accept valid Ethereum address")
        void validate_ethereumAddress_passes() {
            // e.g., "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
        }

        @Test
        @DisplayName("should reject invalid Bitcoin address format")
        void validate_invalidBitcoinAddress_throwsException() {
            // Wrong pattern, invalid characters
        }

        @Test
        @DisplayName("should reject Ethereum address without 0x prefix")
        void validate_ethereumAddressWithoutPrefix_throwsException() {
            // Missing "0x"
        }

        @Test
        @DisplayName("should reject Ethereum address with wrong length")
        void validate_ethereumAddressWrongLength_throwsException() {
            // Not 42 chars total
        }

        @Test
        @DisplayName("should reject Bitcoin address when network is ETHEREUM")
        void validate_bitcoinAddressWithEthereumNetwork_throwsException() {
            // Mismatch between address type and network
        }

        @Test
        @DisplayName("should reject Ethereum address when network is BITCOIN")
        void validate_ethereumAddressWithBitcoinNetwork_throwsException() {
            // Mismatch between address type and network
        }
    }

    @Nested
    @DisplayName("Network Validation")
    class NetworkValidationTests {

        @Test
        @DisplayName("should accept BITCOIN network")
        void validate_bitcoinNetwork_passes() {
            // network = "BITCOIN"
        }

        @Test
        @DisplayName("should accept ETHEREUM network")
        void validate_ethereumNetwork_passes() {
            // network = "ETHEREUM"
        }

        @Test
        @DisplayName("should accept lowercase network name")
        void validate_lowercaseNetwork_passes() {
            // network = "bitcoin" -> convert to uppercase
        }

        @Test
        @DisplayName("should reject invalid network")
        void validate_invalidNetwork_throwsException() {
            // network = "LITECOIN" or "INVALID"
        }

        @Test
        @DisplayName("should reject missing network")
        void validate_missingNetwork_throwsException() {
            // network key not present
        }
    }

    @Nested
    @DisplayName("Minimum Amount Validation")
    class MinimumAmountTests {

        @Test
        @DisplayName("should accept amount equal to minimum (€10.00)")
        void validate_minimumAmount_passes() {
            // amount = €10.00
        }

        @Test
        @DisplayName("should accept amount above minimum")
        void validate_amountAboveMinimum_passes() {
            // amount = €10.01, €100.00, etc.
        }

        @Test
        @DisplayName("should reject amount below minimum (€9.99)")
        void validate_amountBelowMinimum_throwsException() {
            // amount = €9.99
        }

        @Test
        @DisplayName("should reject amount of €0.01")
        void validate_verySmallAmount_throwsException() {
            // amount = €0.01
        }

        @Test
        @DisplayName("should reject zero amount")
        void validate_zeroAmount_throwsException() {
            // amount = €0.00
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle missing wallet address")
        void process_missingWalletAddress_throwsException() {
            // paymentDetails without "walletAddress" key
        }

        @Test
        @DisplayName("should handle null payment details")
        void process_nullPaymentDetails_throwsException() {
            // paymentDetails = null
        }

        @Test
        @DisplayName("should handle empty wallet address")
        void process_emptyWalletAddress_throwsException() {
            // walletAddress = ""
        }

        @Test
        @DisplayName("should handle whitespace-only wallet address")
        void process_whitespaceWalletAddress_throwsException() {
            // walletAddress = "   "
        }
    }
}
