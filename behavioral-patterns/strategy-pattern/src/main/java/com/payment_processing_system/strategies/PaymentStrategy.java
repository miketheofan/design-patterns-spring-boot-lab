package com.payment_processing_system.strategies;

import java.math.BigDecimal;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;

/**
 * Strategy interface for payment processing.
 * Each implementation handles a specific payment method (credit card, PayPal, crypto, bank transfer).
 */
public interface PaymentStrategy {

    /** Processes the payment with validation, fee calculation, and returns transaction details. */
    PaymentResponse process(PaymentRequest request);

    /** Calculates the processing fee for this payment method. */
    BigDecimal calculateFee(BigDecimal amount);
}
