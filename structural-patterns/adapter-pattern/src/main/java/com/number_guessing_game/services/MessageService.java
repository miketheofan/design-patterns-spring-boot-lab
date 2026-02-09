package com.number_guessing_game.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageService {
    private static final String WELCOME_MESSAGE = "Welcome to the Number Guessing Game!";
    private static final String PROMPT_MESSAGE = "Guess a number between {} and {}";
    private static final String PROMPT_QUIT_MESSAGE = "Type 'QUIT' or 'Q' to exit at any time.";

    private static final String WIN_MESSAGE = "Correct! The number was {}";

    private static final String LOSE_MESSAGE = "Nice try, but {} is not the correct number. Try again:";

    public void printWelcome(int min, int max) {
        log.info(WELCOME_MESSAGE);
        log.info(PROMPT_MESSAGE, min, max);
        log.info(PROMPT_QUIT_MESSAGE);
    }

    public void printWinMessage(int guess) {
        log.info(WIN_MESSAGE, guess);
    }

    public void printLoseMessage(int guess) {
        log.info(LOSE_MESSAGE, guess);
    }
}
