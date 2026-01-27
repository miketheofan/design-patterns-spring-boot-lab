package com.payment_processing_system.strategies;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * PayPal payment strategy implementation.
 * Handles validation, processing, and fee calculation for PayPal payments.
 *
 * Validation rules:
 * - Valid email format
 * - OAuth token present
 *
 * Fee: 3.4% + €0.35 per transaction
 */
@Slf4j
@Component
@AllArgsConstructor
public class PayPalPaymentStrategy implements PaymentStrategy {

    private final PaymentValidationHelper validator;

    private static final String EMAIL_KEY = "email";
    private static final String TOKEN_KEY = "token";

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@gmail\\.com$";
    private static final String WRONG_EMAIL_FORMAT_ERROR_MSG = "Email must be in format: smth@gmail.com";
    private static final String TOKEN_PATTERN = "^Bearer [a-fA-F0-9]{10}$";
    private static final String WRONG_TOKEN_FORMAT_ERROR_MSG = "Token must be in format: Bearer [10 tokens]";

    private static final String INSUFFICIENT_FUNDS_MSG = "Insufficient funds";

    @Override
    public PaymentResponse process(PaymentRequest request) {
        log.info("Processing PayPal payment: amount={}", request.getAmount());

        Map<String, Object> details = request.getPaymentDetails();

        // Extract and validate
        extractAndValidateDetails(details);

        // Simulate processing (10% failure rate)
        validator.simulateRandomFailure(0.1, INSUFFICIENT_FUNDS_MSG);

        // Calculate fee
        BigDecimal fee = calculateFee(request.getAmount());

        log.info("PayPal payment completed successfully");
        return buildPaymentResponse(request, fee);
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        // Fee: 3.4% + €0.35
        BigDecimal percentageFee = amount.multiply(new BigDecimal("0.034"));

        return percentageFee.add(new BigDecimal("0.35"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void extractAndValidateDetails(Map<String, Object> details) {
        String email = validator.getSpecificKey(details, EMAIL_KEY);
        String token = validator.getSpecificKey(details, TOKEN_KEY);

        validateEmail(email);
        validateToken(token);
    }

    private void validateEmail(String email) {
        if (!email.matches(EMAIL_PATTERN)) {
            throw new PaymentValidationException(WRONG_EMAIL_FORMAT_ERROR_MSG);
        }
    }

    private void validateToken(String token) {
        if (!token.matches(TOKEN_PATTERN)) {
            throw  new PaymentValidationException(WRONG_TOKEN_FORMAT_ERROR_MSG);
        }
    }

    private PaymentResponse buildPaymentResponse(PaymentRequest request, BigDecimal fee) {
        return PaymentResponse.builder()
                .status(TransactionStatus.COMPLETED)
                .transactionId(validator.generateTransactionId())
                .netAmount(request.getAmount())
                .fee(fee)
                .grossAmount(request.getAmount().add(fee))
                .method(PaymentMethodsEnum.PAYPAL)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
