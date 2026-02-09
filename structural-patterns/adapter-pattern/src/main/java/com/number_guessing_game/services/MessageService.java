package com.number_guessing_game.services;

import com.number_guessing_game.adapters.MessageDisplay;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageService {
    private final MessageDisplay display;

    private static final String WELCOME_MSG = "Welcome to the Number Guessing Game!";
    private static final String PROMPT_MSG = "Guess a number between %d and %d";
    private static final String PROMPT_QUIT_MSG = "Type 'QUIT' or 'Q' to exit at any time.";

    private static final String WIN_MSG = "🎉 Correct! The number was %d. You WIN!";
    private static final String LOSE_MSG = "❌ Wrong! The number was %d, you guessed %d. Computer WINS!";
    private static final String PLAY_AGAIN_MSG = "\nMake another guess to play again, or type QUIT to exit.";

    public void printWelcome(int min, int max) {
        display.showInfo(WELCOME_MSG);
        display.showInfo(String.format(PROMPT_MSG, min, max));
        display.showInfo(PROMPT_QUIT_MSG);
    }

    public void printWinMessage(int targetNumber) {
        display.showSuccess(String.format(WIN_MSG, targetNumber));
    }

    public void printLoseMessage(int targetNumber, int guess) {
        display.showInfo(String.format(LOSE_MSG, targetNumber, guess));
    }

    public void printPlayAgain() {
        display.showInfo(PLAY_AGAIN_MSG);
    }
}
