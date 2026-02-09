package com.number_guessing_game.services;

import com.number_guessing_game.exceptions.InputOutOfBoundsException;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class GameService {
    private static final int MIN = 1;
    private static final int MAX = 10;

    private final Random random;
    private final MessageService messageService;

    private int targetNumber;
    private int attempts;

    public GameService(Random random, MessageService messageService) {
        this.random = random;
        this.messageService = messageService;
    }

    public void startNewGame() {
        // TODO: Encapsulate this functionality better
        this.targetNumber = random.nextInt(MAX - MIN + 1) + MIN;

        this.attempts = 0;
        messageService.printWelcome(MIN, MAX);
    }

    public void processGuess(int guess) {
        if (guess > MAX || guess < MIN) {
            throw new InputOutOfBoundsException(MIN, MAX);
        }
    }

    public boolean isGameOver(int guess) {
        boolean playerWon = guess == targetNumber;
        if (playerWon) {
            messageService.printWinMessage(guess);
        } else {
            messageService.printLoseMessage(guess);
        }

        return playerWon;
    }
}
