package com.number_guessing_game.services;

import com.number_guessing_game.exceptions.NonNumericInputException;
import com.number_guessing_game.exceptions.QuitGameException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Scanner;

@Service
@AllArgsConstructor
public class InputService {
    private final Scanner scanner;

    private static final List<String> QUIT_COMMANDS = List.of("QUIT", "Q");

    public int readInteger() {
        String input = scanner.nextLine().trim();

        // Check if quit command
        if (QUIT_COMMANDS.stream().anyMatch(cmd -> cmd.equalsIgnoreCase(input))) {
            throw new QuitGameException();
        }

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new NonNumericInputException();
        }
    }
}
