package com.number_guessing_game.domains;

import com.number_guessing_game.enums.Competitors;
import com.number_guessing_game.enums.GuessResult;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GameSession {
    private final int targetNumber;
    private final int min;
    private final int max;
    private final LocalDateTime startTime;

    private Integer userGuess;
    private GuessResult result;
    private LocalDateTime endTime;

    public GameSession(int min, int max, int targetNumber) {
        this.min = min;
        this.max = max;
        this.targetNumber = targetNumber;
        this.startTime = LocalDateTime.now();
    }

    /**
     * Process a single guess. One guess = one complete game.
     * @param guess the user's guess
     * @return WIN if correct, LOSE if incorrect
     */
    public GuessResult processGuess(int guess) {
        this.userGuess = guess;
        this.endTime = LocalDateTime.now();

        if (guess == targetNumber) {
            this.result = GuessResult.WIN;
        } else {
            this.result = GuessResult.LOSE;
        }

        return result;
    }

    /**
     * @return "USER" if user won, "COMPUTER" is user lost
     */
    public Competitors getWinner() {
        return result == GuessResult.WIN ? Competitors.USER : Competitors.COMPUTER;
    }

    public boolean isGameComplete() {
        return result != null;
    }
}
