package com.payment_processing_system.strategies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.enums.TransactionStatus;
import com.payment_processing_system.exceptions.PaymentValidationException;
import com.payment_processing_system.utils.PaymentValidationHelper;

import lombok.AllArgsConstructor;

/**
 * Credit card payment strategy implementation.
 * Handles validation, processing, and fee calculation for credit card payments.
 * 
 * Validation rules:
 * - Card number must pass Luhn algorithm
 * - CVV must be 3-4 digits
 * - Expiry date must not be in the past
 * - Cardholder name required
 * 
 * Fee: 2.9% + €0.30 per transaction
 */
@Slf4j
@Component
@AllArgsConstructor
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private static final String CARD_NUMBER_KEY = "cardNumber";
    private static final String CVV_KEY = "cvv";
    private static final String EXPIRY_DATE_KEY = "expiryDate" ;
    private static final String CARDHOLDER_NAME_KEY = "cardHolderName";

    private static final String CVV_NUMBER_PATTERN = "\\d{3,4}";
    private static final String CVV_NUMBER_WRONG_PATTERN_MSG = "CVV must be 3-4 digits";

    private static final String INSUFFICIENT_FUNDS_MSG = "Insufficient funds";

    private static final String CARD_NUMBER_CLEANUP_PATTERN = "[\\s-]";
    private static final String CARD_NUMBER_LENGTH_PATTERN = "\\d{13,19}";
    private static final String CARD_NUMBER_LENGTH_ERROR_MSG = "Card number must be 13-19 digits";
    private static final String CARD_NUMBER_LUHN_ERROR_MSG = "Invalid card number - failed Luhn check";

    private static final String EXPIRY_DATE_PATTERN = "(0[1-9]|1[0-2])/\\d{4}";
    private static final String EXPIRY_DATE_FORMAT_ERROR_MSG = "Expiry date must be in MM/yyyy format";
    private static final String EXPIRY_DATE_EXPIRED_MSG = "Card has expired";
    private static final String EXPIRY_DATE_INVALID_MSG = "Invalid expiry date format";
    private static final String DATE_FORMATTER_PATTERN = "dd/MM/yyyy";
    private static final String DATE_PREFIX = "01/";

    private static final DateTimeFormatter EXPIRY_DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(DATE_FORMATTER_PATTERN);

    private final PaymentValidationHelper validator;

    @Override
    public PaymentResponse process(PaymentRequest request) {
        log.info("Processing credit card payment: amount={}", request.getAmount());
        Map<String, Object> details = request.getPaymentDetails();
        
        // Extract and validate
        extractAndValidateDetails(details);

        // Simulate processing (10% failure rate)
        validator.simulateRandomFailure(0.1, INSUFFICIENT_FUNDS_MSG);

        // Calculate fee
        BigDecimal fee = calculateFee(request.getAmount());

        log.info("Credit card payment completed successfully");
        return buildPaymentResponse(request, fee);
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        // Fee: 2.9% + €0.30
        BigDecimal percentageFee = amount.multiply(new BigDecimal("0.029"));

        return percentageFee.add(new BigDecimal("0.30"))
            .setScale(2, RoundingMode.HALF_UP); // Round to 2 decimal places
    }

    /**
     * Internal data class for validated credit card details.
     * Only used within this strategy - not exposed externally.
     */
    private record CreditCardDetails(
        String cardNumber,
        String cvv,
        String expiryDate,
        String cardholderName
    ) {}

    /**
     * Extracts and validates credit card details from payment details map.
     */
    private CreditCardDetails extractAndValidateDetails(Map<String, Object> details) {
        String cardNumber = validator.getSpecificKey(details, CARD_NUMBER_KEY);
        String cvv = validator.getSpecificKey(details, CVV_KEY);
        String expiryDate = validator.getSpecificKey(details, EXPIRY_DATE_KEY);
        String cardholderName = validator.getSpecificKey(details, CARDHOLDER_NAME_KEY);

        // Validate
        validator.validatePattern(cvv, CVV_NUMBER_PATTERN, CVV_NUMBER_WRONG_PATTERN_MSG);
        validateExpiryDate(expiryDate);
        validateCardNumber(cardNumber);

        return new CreditCardDetails(cardNumber, cvv, expiryDate, cardholderName);
    }

    /**
     * Validates card number using Luhn algorithm.
     */
    private void validateCardNumber(String cardNumber) {
        // Remove spaces and dashes
        String cleanNumber = cardNumber.replaceAll(CARD_NUMBER_CLEANUP_PATTERN, "");

        // Check length (13-19 digits for most cards)
        if (!cleanNumber.matches(CARD_NUMBER_LENGTH_PATTERN)) {
            throw new PaymentValidationException(CARD_NUMBER_LENGTH_ERROR_MSG);
        }

        // Luhn algorithm check
        if (!passesLuhnCheck(cleanNumber)) {
            throw new PaymentValidationException(CARD_NUMBER_LUHN_ERROR_MSG);
        }
    }

    /**
     * Implements Luhn algorithm for card number validation.
     * 
     * Algorithm:
     * 1. Starting from right, double every second digit
     * 2. If doubled digit > 9, subtract 9
     * 3. Sum all digits
     * 4. Valid if sum % 10 == 0
     */
    private boolean passesLuhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        // Iterate from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }

    /**
     * Validates expiry date format (MM/yyyy) and ensures not expired.
     */
    private void validateExpiryDate(String expiryDate) {
        // Validate format (MM/yyyy)
        if (!expiryDate.matches(EXPIRY_DATE_PATTERN)) {
            throw new PaymentValidationException(EXPIRY_DATE_FORMAT_ERROR_MSG);
        }
        
        try {
            // Parse expiry date - card is valid through the last day of the month
            LocalDate expiry = LocalDate.parse(DATE_PREFIX + expiryDate, EXPIRY_DATE_FORMATTER);
            
            // Get last day of the expiry month
            LocalDate lastDayOfMonth = expiry.withDayOfMonth(expiry.lengthOfMonth());
            
            // Check if expired
            if (lastDayOfMonth.isBefore(LocalDate.now())) {
                throw new PaymentValidationException(EXPIRY_DATE_EXPIRED_MSG);
            }
            
        } catch (DateTimeParseException e) {
            throw new PaymentValidationException(
                EXPIRY_DATE_INVALID_MSG, e
            );
        }
    }

    private PaymentResponse buildPaymentResponse(PaymentRequest request, BigDecimal fee) {
        return PaymentResponse.builder()
                .status(TransactionStatus.COMPLETED)
                .transactionId(validator.generateTransactionId())
                .netAmount(request.getAmount())
                .fee(fee)
                .grossAmount(request.getAmount().add(fee))
                .method(PaymentMethodsEnum.CREDIT_CARD)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
