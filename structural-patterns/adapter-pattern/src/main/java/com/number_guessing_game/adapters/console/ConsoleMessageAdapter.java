package com.number_guessing_game.adapters.console;

import com.number_guessing_game.adapters.MessageDisplay;
import org.springframework.stereotype.Component;

/**
 * Adapter: Adapts System.out/err (Adaptee) to MessageDisplay interface (Target)
 * This allows the game to display messages without knowing it's a console.
 */
@Component
public class ConsoleMessageAdapter implements MessageDisplay {
    @Override
    public void showInfo(String message) {
        System.out.println(message);
    }

    @Override
    public void showError(String message) {
        System.err.println("❌ " + message);
    }

    @Override
    public void showSuccess(String message) {
        System.out.println("✅ " + message);
    }
}
