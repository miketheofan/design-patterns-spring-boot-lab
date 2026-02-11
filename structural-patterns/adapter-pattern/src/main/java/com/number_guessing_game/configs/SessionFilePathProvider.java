package com.number_guessing_game.configs;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Provides a session-specific file path for CSV statistics storage.
 * <p>
 *     Generates a timestamped filename at applciation startup, ensuring all games
 *     in a single session are written to the same CSV file. Each application run
 *     creates a new file with a unique name.
 * </p>
 * Example output: {@code stats/stats-20260211153045.csv}
 */
@Component
public class SessionFilePathProvider {
    @Value("${game.statistics.directory}")
    private String baseDir;
    private static final DateTimeFormatter FILENAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String sessionFilePath;

    /**
     * Initializes the session file path with current timestamp.
     * Called automatically by Spring after bean construction.
     */
    @PostConstruct
    public void initialize() {
        String timestamp = LocalDateTime.now().format(FILENAME_FORMAT);
        this.sessionFilePath = baseDir + "/stats-" + timestamp + ".csv";
    }

    /**
     * Returns the session-specific CSV file path.
     * <p>
     *     The path is generated once at application startup and remains constant
     *     throughout the session.
     * </p>
     * @return the absolute path to the session's CSV file
     */
    public String getSessionFilePath() {
        return sessionFilePath;
    }
}
