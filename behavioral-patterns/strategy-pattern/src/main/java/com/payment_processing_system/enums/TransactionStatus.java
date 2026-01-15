package com.payment_processing_system.enums;

/**
 * Represents the status of a payment transaction.
 * Used to indicate the outcome of payment processing.
 */
public enum TransactionStatus {

    /** Transaction completed successfully */
    COMPLETED,

    /** Transaction failed (validation or processing error) */
    FAILED,

    /** Transaction is pending (optional - for async processing) */
    PENDING
}
