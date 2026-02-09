package com.number_guessing_game.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GameExceptionHandler {
    public void handle(GameException ex) {
        log.error(ex.getErrorMessage());
        log.info(ex.getPromptMessage());
    }
}
