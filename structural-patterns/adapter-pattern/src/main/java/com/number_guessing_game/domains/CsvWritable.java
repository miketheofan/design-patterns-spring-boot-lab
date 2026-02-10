package com.number_guessing_game.domains;

/**
 * Interface for domain objects that can be serialized to CSV format.
 * <p>
 *     Implementing classes define their own CSV representation
 *     including header and row formatting.
 * </p>
 */
public interface CsvWritable {
    /**
     * Returns the CSV header row for this object's fields.
     *
     * @return a comma-separated string of column names
     */
    String csvHeader();

    /**
     * Returns this object's data as a CSV-formatted row.
     * Values containing commas or quotes should be properly escaped.
     *
     * @return a comma separated string of field values
     */
    String toCsvRow();
}
