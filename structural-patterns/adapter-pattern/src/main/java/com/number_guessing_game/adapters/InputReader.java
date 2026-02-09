package com.number_guessing_game.adapters;

/**
 * Target interface - abstracts input reading from any source
 * (console, GUI, web, file, network, etc.)
 */
public interface InputReader {
    /**
     * Reads an integer from the input source.
     * @return the integer value
     */
    int readInteger();

    /**
     * Reads a string from the input source.
     * @return the trimmed string
     */
    String readString();
}
