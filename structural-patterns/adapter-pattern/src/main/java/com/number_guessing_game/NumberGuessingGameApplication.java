package com.number_guessing_game;

import com.number_guessing_game.exceptions.*;
import com.number_guessing_game.services.GameService;
import com.number_guessing_game.services.InputService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
@AllArgsConstructor
public class NumberGuessingGameApplication implements CommandLineRunner {
    private final GameService gameService;
    private final InputService inputService;
    private final GameExceptionHandler exceptionHandler;
    private final Scanner scanner;

	public static void main(String[] args) {
		SpringApplication.run(NumberGuessingGameApplication.class, args);
	}

    @Override
    public void run(String... args) {
        try {
            gameService.startNewGame();

            while (true) {
                try {
                    int guess = inputService.readInteger();
                    gameService.processGuess(guess);

                    if (gameService.isGameOver(guess)) {
                        break;
                    }
                } catch (QuitGameException ex) {
                    exceptionHandler.handle(ex);
                    break;
                } catch (GameException ex) {
                    exceptionHandler.handle(ex);
                }
            }
        } finally {
            scanner.close();
        }
    }
}
