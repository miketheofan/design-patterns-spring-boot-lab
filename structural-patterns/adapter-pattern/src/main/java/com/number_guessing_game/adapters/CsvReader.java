package com.number_guessing_game.adapters;

import com.number_guessing_game.domains.GameResults;
import com.number_guessing_game.exceptions.CsvReadException;

import java.util.List;

/**
 * Abstraction for read CSV data from persistent storage.
 * Implementations handle the actual I/O operations.
 */
public interface CsvReader {
    /**
     * Reads CSV data from a specified file.
     * @throws CsvReadException if reading fails
     */
    List<GameResults> readAll();
}
