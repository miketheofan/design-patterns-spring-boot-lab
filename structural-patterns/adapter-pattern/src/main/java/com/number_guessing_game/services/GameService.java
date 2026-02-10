package com.number_guessing_game.services;

import com.number_guessing_game.domains.GameSession;
import com.number_guessing_game.exceptions.InputOutOfBoundsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class GameService {
    @Value("${game.range.min}")
    private int min;

    @Value("${game.range.max}")
    private int max;

    private final Random random;

    public GameService(Random random) {
        this.random = random;
    }

    /** Creates a new game session with a random target number.
     * Each call creates a NEW game.
     */
    public GameSession createNewGame() {
        // TODO: Encapsulate this functionality better
        int targetNumber = random.nextInt(max - min + 1) + min;
        return new GameSession(min, max, targetNumber);
    }

    /**
     * Validates that a guess is within the valid range.
     * @throws InputOutOfBoundsException if invalid.
     */
    public void validateGuess(int guess) {
        if (guess > max || guess < min) {
            throw new InputOutOfBoundsException(min, max);
        }
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
