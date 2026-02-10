package com.number_guessing_game.adapters;

import com.number_guessing_game.domains.CsvWritable;
import com.number_guessing_game.exceptions.CsvWriteException;

import java.util.List;

/**
 * Abstraction for writing CSV data to persistent storage.
 * Implementations handle the actual I/O operations.
 */
public interface CsvWriter {
    /**
     * Writes a single CSV-writable item to the specified file.
     * Creates file if it doesn't exist, appends if it does.
     * Header is written only for new files.
     *
     * @param filePath the target CSV file path
     * @param item the item to write
     * @throws CsvWriteException if writing fails
     */
    void write(String filePath, CsvWritable item);

    /**
     * Writes multiple CSV-writable items to the specified file.
     * Creates file if it doesn't exist, appends if it does.
     * Header is written only for new files.
     *
     * @param filePath the target CSV file path
     * @param items the items to write
     * @throws CsvWriteException if writing fails
     */
    void writeAll(String filePath, List<? extends CsvWritable> items);
}
