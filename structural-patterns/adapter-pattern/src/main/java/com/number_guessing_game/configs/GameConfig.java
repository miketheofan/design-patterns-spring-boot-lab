package com.number_guessing_game.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;
import java.util.Scanner;

@Configuration
public class GameConfig {
    @Bean
    public Random random() {
        return new Random();
    }

    @Bean(destroyMethod = "close")
    public Scanner scanner() {
        return new Scanner(System.in);
    }
}
