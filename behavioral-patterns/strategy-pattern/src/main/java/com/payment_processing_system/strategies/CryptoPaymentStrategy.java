package com.payment_processing_system.strategies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

import com.payment_processing_system.enums.CryptoNetworkEnum;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.enums.TransactionStatus;
import com.payment_processing_system.exceptions.PaymentValidationException;
import com.payment_processing_system.utils.PaymentValidationHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;

/**
 * Crypto payment strategy implementation.
 * Handles validation, processing, and fee calculation for crypto payments.
 *
 * Validation rules:
 * - Valid wallet address format (Bitcoin or Ethereum)
 * - Network specified (BITCOIN/ETHEREUM)
 * - Amount meets minumum threshold (€10)
 *
 * Fee: 1.0% + network gas fees per transaction
 */
@Slf4j
@Component
@AllArgsConstructor
public class CryptoPaymentStrategy implements PaymentStrategy {

    private final PaymentValidationHelper validator;

    private static final String NETWORK_KEY = "network";
    private static final String WALLET_ADDRESS_KEY = "walletAddress";

    private static final String BITCOIN_ADDRESS_PATTERN = "^(1|3|bc1)[a-zA-HJ-NP-Z0-9]{25,62}$";
    private static final String ETHEREUM_ADDRESS_PATTERN = "^0x[a-fA-F0-9]{40}$";

    private static final String BITCOIN_ADDRESS_ERROR_MSG = "Invalid Bitcoin address format";
    private static final String ETHEREUM_ADDRESS_ERROR_MSG = "Invalid Ethereum address format";
    private static final String MINIMUM_AMOUNT_ERROR_MSG = "Cryptocurrency payment minimum is €10.00";
    private static final String INVALID_NETWORK_ERROR_MSG = "Network type is not supported";
    private static final String HIGH_NETWORK_CONG_ERR_MSG = "High network congestion - try again";

    @Override
    public PaymentResponse process(PaymentRequest request) {
        log.info("Processing crypto payment: amount={}", request.getAmount());

        Map<String, Object> details = request.getPaymentDetails();

        // Extract and validate
        extractAndValidateDetails(details, request.getAmount());

        // Simulate network congestion (15% probability)
        checkNetworkCongestion();

        // Calculate fee with gas
        BigDecimal fee = calculateFee(request.getAmount());

        log.info("Crypto payment completed successfully");
        return buildPaymentRespone(request, fee);
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        // Fee: 1.0% + network gas fee
        BigDecimal percentageFee = amount.multiply(new BigDecimal("0.01"));
        BigDecimal gasFee = calculateNetworkGasFee();
        log.debug("Network gas fee calculated: {}", gasFee);

        return percentageFee.add(gasFee)
                .setScale(2, RoundingMode.HALF_UP); // Round to 2 decimal places
    }

    /**
     * Calculates network gas fee based on simulated congestion levels.
     * Package-private for testing purposes.
     */
    protected BigDecimal calculateNetworkGasFee() {
        double random = Math.random();
        BigDecimal gasFee;

        if (random < 0.5) {
            // 50% - Low congestion
            gasFee = new BigDecimal("1.00");
        } else if (random < 0.85) {
            // 35% - Medium congestion
            gasFee = new BigDecimal("2.50");
        } else {
            // 15% - High congestion
            gasFee = new BigDecimal("5.00");
        }

        return gasFee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Simulates network congestion check with 15% probability.
     * Package-private for testing purposes.
     */
    protected void checkNetworkCongestion() {
        double random = Math.random();
        if (random < 0.15) {
            validator.simulateRandomFailure(0.3, HIGH_NETWORK_CONG_ERR_MSG);
        }
    }

    private void extractAndValidateDetails(Map<String, Object> details, BigDecimal amount) {
        String walletAddress = validator.getSpecificKey(details, WALLET_ADDRESS_KEY);
        String networkStr = validator.getSpecificKey(details, NETWORK_KEY);

        final BigDecimal minAmount = BigDecimal.valueOf(10);

        // Validate minimum amount
        if (amount.compareTo(minAmount) < 0) {
            throw new PaymentValidationException(MINIMUM_AMOUNT_ERROR_MSG);
        }

        // Parse and validate network
        CryptoNetworkEnum network;
        try {
            network = CryptoNetworkEnum.valueOf(networkStr.toUpperCase());
        } catch(IllegalArgumentException e) {
            throw new PaymentValidationException(INVALID_NETWORK_ERROR_MSG);
        }

        // Validate walled address based on network
        validateWalletAddress(walletAddress, network);
    }

    private void validateWalletAddress(String address, CryptoNetworkEnum network) {
        switch (network) {
            case BITCOIN -> {
                if (!address.matches(BITCOIN_ADDRESS_PATTERN)) {
                    throw new PaymentValidationException(BITCOIN_ADDRESS_ERROR_MSG);
                }
            }
            case ETHEREUM -> {
                if (!address.matches(ETHEREUM_ADDRESS_PATTERN)) {
                    throw new PaymentValidationException(ETHEREUM_ADDRESS_ERROR_MSG);
                }
            }
        }
    }

    private PaymentResponse buildPaymentRespone(PaymentRequest request, BigDecimal fee) {
        return PaymentResponse.builder()
                .status(TransactionStatus.COMPLETED)
                .transactionId(validator.generateTransactionId())
                .netAmount(request.getAmount())
                .fee(fee)
                .grossAmount(request.getAmount().add(fee))
                .method(PaymentMethodsEnum.CRYPTO)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
