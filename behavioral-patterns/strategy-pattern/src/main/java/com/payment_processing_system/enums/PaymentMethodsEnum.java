package com.payment_processing_system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of supported payment methods in the system.
 * Each payment method represents a distinct payment processing strategy.
 * 
 * <p>This enum serves as an identifier for payment methods and is used
 * in conjunction with the Strategy pattern to select the appropriate
 * payment processing implementation.</p>
 * 
 * @see com.payment_processing_system.strategy.PaymentStrategy
 * @author Payment Processing System
 * @version 1.0
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum PaymentMethodsEnum {
    
    /** Credit card payment method (Visa, MasterCard, etc.) */
    CREDIT_CARD("Credit Card"),
    
    /** PayPal online payment method */
    PAYPAL("PayPal"),
    
    /** Cryptocurrency payment method (Bitcoin, Ethereum) */
    CRYPTO("Cryptocurrency"),
    
    /** Direct bank transfer method */
    BANK_TRANSFER("Bank Transfer");

    /** The human-readable display name of the payment method */
    private final String name;
}

