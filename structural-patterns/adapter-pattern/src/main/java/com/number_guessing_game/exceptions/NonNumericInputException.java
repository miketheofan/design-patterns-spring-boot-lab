package com.number_guessing_game.exceptions;

public class NonNumericInputException extends GameException {
    private static final String ERROR_TEMPLATE = "Invalid input! Please enter a numeric value.";
    private static final String PROMPT = "Try again:";

    public NonNumericInputException() {
        super(ERROR_TEMPLATE, PROMPT);
    }}
