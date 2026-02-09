package com.number_guessing_game.exceptions;

import com.number_guessing_game.adapters.MessageDisplay;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GameExceptionHandler {
    private final MessageDisplay display;

    public void handle(GameException ex) {
        display.showError(ex.getMessage());
        display.showInfo(ex.getPromptMessage());
    }
}
