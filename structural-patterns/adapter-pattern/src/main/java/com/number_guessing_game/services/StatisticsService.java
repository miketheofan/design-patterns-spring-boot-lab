package com.number_guessing_game.services;

import com.number_guessing_game.adapters.CsvWriter;
import com.number_guessing_game.domains.CsvWritable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for recording and managing game statistics.
 * Delegates CSV persistence to CsvWriter adapter.
 * Statistics file path is configured via application.yaml
 */
@Service
public class StatisticsService {

    @Value("${game.statistics.file-path}")
    private String statsFilePath;

    private final CsvWriter csvWriter;

    public StatisticsService(CsvWriter csvWriter) {
        this.csvWriter = csvWriter;
    }

    /**
     * Records a single game result to the configured statistics file.
     *
     * @param item the game result to record
     */
    public void recordGame(CsvWritable item) {
        csvWriter.write(statsFilePath, item);
    }

    /**
     * Records multiple game results to the configured statistics file.
     *
     * @param items the game results to record
     */
    public void recordGames(List<? extends CsvWritable> items) {
        csvWriter.writeAll(statsFilePath, items);
    }
}
