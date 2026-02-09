package com.number_guessing_game.adapters.console;

import com.number_guessing_game.adapters.InputReader;
import com.number_guessing_game.exceptions.NonNumericInputException;
import com.number_guessing_game.exceptions.QuitGameException;
import org.springframework.stereotype.Component;

import java.util.Scanner;
import java.util.Set;

/**
 * Adapter: Adapts Scanner (Adaptee) to InputReader interface (Target)
 * This allows the game to read input without knowing it's from a console.
 */
@Component
public class ConsoleInputAdapter implements InputReader {
    private final Scanner scanner;
    private static final Set<String> QUIT_COMMANDS = Set.of("QUIT", "Q");

    public ConsoleInputAdapter(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public int readInteger() {
        String input = readString();

        // Check quit command
        if (QUIT_COMMANDS.contains(input.toUpperCase())) {
            throw new QuitGameException();
        }

        // Parse to integer
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new NonNumericInputException();
        }
    }

    @Override
    public String readString() {
        return scanner.nextLine().trim();
    }
}
