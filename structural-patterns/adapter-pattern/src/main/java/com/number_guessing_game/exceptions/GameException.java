package com.number_guessing_game.exceptions;

import lombok.Getter;

@Getter
public class GameException extends RuntimeException {
    private final String promptMessage;

    protected GameException(String errorMessage, String promptMessage) {
        super(errorMessage);
        this.promptMessage = promptMessage;
    }

    protected GameException(String errorMessage, String promptMessage, Throwable cause) {
        super(errorMessage, cause);
        this.promptMessage = promptMessage;
    }
}
