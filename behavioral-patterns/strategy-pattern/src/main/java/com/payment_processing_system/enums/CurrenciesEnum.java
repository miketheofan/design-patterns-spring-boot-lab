package com.payment_processing_system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents supported currencies in the payment processing system.
 * Each currency includes a display name and symbol for formatting.
 * 
 * @author Payment Processing System
 * @version 1.0
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum CurrenciesEnum {
    
    /** Euro currency */
    EUR("Euro", "€"),
    
    /** United States Dollar */
    USD("US Dollar", "$"),
    
    /** British Pound Sterling */
    GBP("British Pound", "£");

    /** The human-readable name of the currency */
    private final String displayName;
    
    /** The currency symbol used for display purposes */
    private final String symbol;
}

