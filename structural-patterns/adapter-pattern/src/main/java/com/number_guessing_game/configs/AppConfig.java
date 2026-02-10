package com.number_guessing_game.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.Scanner;

/**
 * Placeholder for manual Bean configurations used throughout the project
 */
@Configuration
public class AppConfig {
    @Bean
    public Random random() {
        return new Random();
    }

    @Bean(destroyMethod = "close")
    public Scanner scanner() {
        return new Scanner(System.in);
    }
}
