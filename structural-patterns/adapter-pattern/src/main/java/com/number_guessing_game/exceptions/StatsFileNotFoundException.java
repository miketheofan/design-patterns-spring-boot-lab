package com.number_guessing_game.exceptions;

public class StatsFileNotFoundException extends GameException {
    private static final String ERROR_TEMPLATE = "Could not find stats file in: %s";
    private static final String PROMPT = "Your game progress could not be summarized, but you can continue playing.";

    public StatsFileNotFoundException(String filePath) {
        super(String.format(ERROR_TEMPLATE, filePath), PROMPT);
    }

    public StatsFileNotFoundException(Throwable cause) {
        super(ERROR_TEMPLATE, PROMPT, cause);
    }
}
