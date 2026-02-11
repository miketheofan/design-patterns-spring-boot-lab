package com.number_guessing_game.services;

import com.number_guessing_game.adapters.CsvReader;
import com.number_guessing_game.adapters.CsvWriter;
import com.number_guessing_game.domains.CsvWritable;
import com.number_guessing_game.domains.GameResults;
import com.number_guessing_game.domains.GameStatistics;
import com.number_guessing_game.utils.StatisticsCalculator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for recording and managing game statistics.
 * Delegates CSV persistence to CsvWriter adapter.
 * Statistics file path is configured via application.yaml
 */
@Service
@AllArgsConstructor
public class StatisticsService {
    private final CsvWriter csvWriter;
    private final CsvReader csvReader;

    /**
     * Records a single game result to the configured statistics file.
     *
     * @param item the game result to record
     */
    public void recordGame(CsvWritable item) {
        csvWriter.write(item);
    }

    /**
     * Records multiple game results to the configured statistics file.
     *
     * @param items the game results to record
     */
    public void recordGames(List<? extends CsvWritable> items) {
        csvWriter.writeAll(items);
    }

    public GameStatistics calculateStatistics() {
        List<GameResults> results = csvReader.readAll();
        return StatisticsCalculator.calculate(results);
    }
}
