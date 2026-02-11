package com.number_guessing_game.adapters;

import com.number_guessing_game.configs.SessionFilePathProvider;
import com.number_guessing_game.domains.GameResults;
import com.number_guessing_game.enums.Competitors;
import com.number_guessing_game.exceptions.StatsFileNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Adapter that bridges CsvReader interface with Java's FileReader.
 * Handles file existence checks, and data retrieval.
 *
 * This is the ADAPTER in the Adapter Pattern:
 * - Target Interface: CsvReader
 * - Adaptee: FileReader (Java I/O)
 * - Adapter: FileCsvReaderAdapter (this class)
 */
@Component
@AllArgsConstructor
public class FileCsvReaderAdapter implements CsvReader {
    private final SessionFilePathProvider pathProvider;

    private static final DateTimeFormatter CSV_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<GameResults> readAll() {
        String statsFilePath = pathProvider.getSessionFilePath();
        try (BufferedReader reader = new BufferedReader(new FileReader(statsFilePath))) {
            String header = reader.readLine(); // skip header

            String line;
            List<GameResults> gameResults = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                GameResults result = mapCsvRowToGameResults(line);
                gameResults.add(result);
            }

            return gameResults;
        } catch (IOException ex) {
            throw new StatsFileNotFoundException(statsFilePath);
        }
    }

    /**
     * Maps single line of CSV data to GameResults domain object
     */
    private GameResults mapCsvRowToGameResults(String line) {
        String[] fields = line.split(",");
        return GameResults.builder()
                .dateTime(LocalDateTime.parse(fields[0], CSV_DATE_FORMAT))
                .winner(Competitors.valueOf(fields[1]))
                .winningNumber(Integer.parseInt(fields[2]))
                .build();
    }
}
