package com.number_guessing_game.exceptions;

public class CsvReadException extends GameException {
    private static final String ERROR_TEMPLATE = "Failed to read game statistics from file.";
    private static final String PROMPT = "Your game progress could not be summarized, but you can continue playing.";

    public CsvReadException() {
        super(ERROR_TEMPLATE, PROMPT);
    }

    public CsvReadException(Throwable cause) {
        super(ERROR_TEMPLATE, PROMPT, cause);
    }
}
