package com.number_guessing_game.services;

import com.number_guessing_game.domains.GameSession;
import com.number_guessing_game.exceptions.InputOutOfBoundsException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@AllArgsConstructor
public class GameService {
    private static final int MIN = 1;
    private static final int MAX = 10;

    private final Random random;

    /** Creates a new game session with a random target number.
     * Each call creates a NEW game.
     */
    public GameSession createNewGame() {
        // TODO: Encapsulate this functionality better
        int targetNumber = random.nextInt(MAX - MIN + 1) + MIN;
        return new GameSession(MIN, MAX, targetNumber);
    }

    /**
     * Validates that a guess is within the valid range.
     * @throws InputOutOfBoundsException if invalid.
     */
    public void validateGuess(int guess) {
        if (guess > MAX || guess < MIN) {
            throw new InputOutOfBoundsException(MIN, MAX);
        }
    }

    public int getMin() {
        return MIN;
    }

    public int getMax() {
        return MAX;
    }
}
