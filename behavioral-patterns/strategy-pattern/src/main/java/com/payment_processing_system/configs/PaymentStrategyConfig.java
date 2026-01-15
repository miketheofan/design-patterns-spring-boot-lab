package com.payment_processing_system.configs;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.strategies.BankTransferPaymentStrategy;
import com.payment_processing_system.strategies.CreditCardPaymentStrategy;
import com.payment_processing_system.strategies.CryptoPaymentStrategy;
import com.payment_processing_system.strategies.PayPalPaymentStrategy;
import com.payment_processing_system.strategies.PaymentStrategy;

/**
 * Configuration for payment strategy mapping.
 * Maps each payment method enum to its corresponding strategy implementation.
 */
@Configuration
public class PaymentStrategyConfig {

    @Bean
    public Map<PaymentMethodsEnum, PaymentStrategy> paymentStrategies(
        CreditCardPaymentStrategy creditCardStrategy, 
        CryptoPaymentStrategy cryptoStrategy, 
        BankTransferPaymentStrategy bankTransferStrategy, 
        PayPalPaymentStrategy payPalStrategy
    ) {
        return Map.of(
            PaymentMethodsEnum.CREDIT_CARD, creditCardStrategy,
            PaymentMethodsEnum.CRYPTO, cryptoStrategy,
            PaymentMethodsEnum.BANK_TRANSFER, bankTransferStrategy,
            PaymentMethodsEnum.PAYPAL, payPalStrategy
        );

    }
}
