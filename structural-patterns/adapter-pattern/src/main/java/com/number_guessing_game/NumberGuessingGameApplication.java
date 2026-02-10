package com.number_guessing_game;

import com.number_guessing_game.adapters.InputReader;
import com.number_guessing_game.domains.GameResults;
import com.number_guessing_game.domains.GameSession;
import com.number_guessing_game.enums.GuessResult;
import com.number_guessing_game.exceptions.*;
import com.number_guessing_game.services.GameService;
import com.number_guessing_game.services.MessageService;
import com.number_guessing_game.services.StatisticsService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
@AllArgsConstructor
public class NumberGuessingGameApplication implements CommandLineRunner {
    private final GameService gameService;
    private final MessageService messageService;
    private final StatisticsService statisticsService;

    private final InputReader inputReader;
    private final GameExceptionHandler exceptionHandler;
    private final Scanner scanner;

	public static void main(String[] args) {
		SpringApplication.run(NumberGuessingGameApplication.class, args);
	}

    @Override
    public void run(String... args) {
        try {
            messageService.printWelcome(gameService.getMin(), gameService.getMax());

            // Multi-game loop - each iteration is ONE complete game (one guess)
            while (true) {
                // Create a NEW game for each guess
                GameSession session = gameService.createNewGame();

                try {
                    // Read user input
                    int guess = inputReader.readInteger();

                    // Validate range
                    gameService.validateGuess(guess);

                    // Process guess (one guess = one complete game)
                    GuessResult result = session.processGuess(guess);

                    // Display result
                    if (result == GuessResult.WIN) {
                        messageService.printWinMessage(session.getTargetNumber());
                    } else {
                        messageService.printLoseMessage(session.getTargetNumber(), guess);
                    }

                    // Save session to CSV
                    GameResults gameResults = GameResults.builder()
                            .dateTime(session.getEndTime())
                            .winner(session.getWinner())
                            .winningNumber(session.getTargetNumber())
                            .build();

                    statisticsService.recordGame(gameResults);

                    messageService.printPlayAgain();
                } catch (QuitGameException ex) {
                    exceptionHandler.handle(ex);

                    // TODO: Display statistics here
                    // statisticsService.displaySummary();

                    break;
                } catch (GameException ex) {
                    exceptionHandler.handle(ex);
                    // Exception doesn't end the game loop, just retry
                }
            }
        } finally {
            scanner.close();
        }
    }
}
