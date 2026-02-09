package com.number_guessing_game.exceptions;

public class InputOutOfBoundsException extends GameException {
    private static final String ERROR_TEMPLATE = "Input must be between %d and %d";
    private static final String PROMPT = "Try again with a different number:";

    public InputOutOfBoundsException(int min, int max) {
        super(String.format(ERROR_TEMPLATE, min, max), PROMPT);
    }
}
