package com.number_guessing_game.exceptions;

public class CsvWriteException extends GameException {
    private static final String ERROR_TEMPLATE = "Failed to save game statistics to file.";
    private static final String PROMPT = "Your game progress could not be recorded, but you can continue playing.";

    public CsvWriteException() {
        super(ERROR_TEMPLATE, PROMPT);
    }

    public CsvWriteException(Throwable cause) {
        super(ERROR_TEMPLATE, PROMPT, cause);
    }
}
