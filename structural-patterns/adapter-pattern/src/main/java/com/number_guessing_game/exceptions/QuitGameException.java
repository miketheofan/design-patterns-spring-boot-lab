package com.number_guessing_game.exceptions;

public class QuitGameException extends GameException {
    private static final String ERROR_TEMPLATE = "Game ended by user.";
    private static final String PROMPT = "Thanks for playing!";

    public QuitGameException() {
        super(ERROR_TEMPLATE, PROMPT);
    }}
