package com.payment_processing_system.domains;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.payment_processing_system.enums.CurrenciesEnum;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.enums.TransactionStatus;

import lombok.Builder;
import lombok.Data;

/**
 * Response object returned after payment processing attempt.
 * Contains transaction details for successful payments or error information for failures.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {

    /** Transaction status (COMPLETED, FAILED, PENDING) */
    private TransactionStatus status;

    /** Unique transaction identifier (only for successfull payments) */
    private String transactionId;

    /** The payment amount processed */
    private BigDecimal netAmount;

    /** The currency used */
    private CurrenciesEnum currency;

    /**Processing fee charged */
    private BigDecimal fee;

    /** Total amount including fees */
    private BigDecimal grossAmount;

    /** Payment method used */
    private PaymentMethodsEnum method;

    /** Timestamp of the transaction */
    private LocalDateTime timestamp;

    /** Single error message (for processing errors) */
    private String error;

    /** List of validation errors (for validation failures) */
    private List<String> errors;
}
