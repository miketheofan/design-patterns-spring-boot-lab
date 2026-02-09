package com.number_guessing_game.adapters;

/**
 * Target interface - abstracts message display to any output
 * (console, GUI, web response, file, etc.)
 */
public interface MessageDisplay {
    /**
     * Displays an informational message to the user.
     */
    void showInfo(String message);

    /**
     * Displays an error message to the user.
     */
    void showError(String message);

    /**
     * Displays a success message to the user.
     */
    void showSuccess(String message);
}
