package com.payment_processing_system.strategies;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

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
 * Bank Transfer payment strategy implementation.
 * Handles validation, processing, and fee calculation for bank transfer payments.
 *
 * Validation rules:
 * - Valid IBAN format with checksum
 * - Valid SWIFT/BIC code
 * - Account holder name required
 *
 */
@Slf4j
@Component
@AllArgsConstructor
public class BankTransferPaymentStrategy implements PaymentStrategy {

    private static final String IBAN_KEY = "iban";
    private static final String BIC_CODE_KEY = "bicCode";
    private static final String CARD_HOLDER_NAME_KEY = "card_holder_name";

    private static final String IBAN_PATTERN = "^GR\\d+$";
    private static final String WRONG_IBAN_FORMAT_ERROR_MSG = "IBAN must be in format: GR[tokens]";
    private static final String BIC_CODE_PATTERN = "^\\d{5}$";
    private static final String WRONG_BIC_CODE_ERROR_MSG = "BIC Code is not supported";

    private static final String INSUFFICIENT_FUNDS_MSG = "Insufficient funds";

    private final PaymentValidationHelper validator;

    @Override
    public PaymentResponse process(PaymentRequest request) {
        log.info("Processing bank transfer payment: amount={}", request.getAmount());
        Map<String, Object> details = request.getPaymentDetails();

        // Extract and validate
        extractAndValidateDetails(details);

        // Simulate processing (10% failure rate)
        validator.simulateRandomFailure(0.1, INSUFFICIENT_FUNDS_MSG);

        // Calculate fee
        BigDecimal fee = calculateFee(request.getAmount());

        log.info("Bank transfer payment completed successfully");
        return buildPaymentResponse(request, fee);
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        // No-fee
        return new BigDecimal("0.0");
    }

    private void extractAndValidateDetails(Map<String, Object> details) {
        String iban = validator.getSpecificKey(details, IBAN_KEY);
        String bicCode = validator.getSpecificKey(details, BIC_CODE_KEY);
        String cardHolderName = validator.getSpecificKey(details, CARD_HOLDER_NAME_KEY);

        validateIban(iban);
        validateBicCode(bicCode);
    }

    private void validateIban(String iban) {
        if (!iban.matches(IBAN_PATTERN)) {
            throw new PaymentValidationException(WRONG_IBAN_FORMAT_ERROR_MSG);
        }
    }

    private void validateBicCode(String bicCode) {
        if (!bicCode.matches(BIC_CODE_PATTERN)) {
            throw new PaymentValidationException(WRONG_BIC_CODE_ERROR_MSG);
        }
    }

    private PaymentResponse buildPaymentResponse(PaymentRequest request, BigDecimal fee) {
        return PaymentResponse.builder()
                .status(TransactionStatus.COMPLETED)
                .transactionId(validator.generateTransactionId())
                .netAmount(request.getAmount())
                .fee(fee)
                .grossAmount(request.getAmount().add(fee))
                .method(PaymentMethodsEnum.BANK_TRANSFER)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
