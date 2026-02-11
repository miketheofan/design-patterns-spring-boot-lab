package com.number_guessing_game.adapters;

import com.number_guessing_game.domains.CsvWritable;
import com.number_guessing_game.exceptions.CsvWriteException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/** Adapter that bridges CsvWriter interface with Java's FileWriter.
 * Handles file existence checks, header writing, and resource management.
 *
 * This is the ADAPTER in the Adapter Pattern:
 * - Target Interface: CsvWriter
 * - Adaptee: FileWriter (Java I/O)
 * - Adapter: CsvFileAdapter (this class)
 */
@Component
public class FileCsvWriterAdapter implements CsvWriter {
    @Value("${game.statistics.file-path}")
    private String statsFilePath;

    @Override
    public void write(CsvWritable item) {
        writeAll(List.of(item));
    }

    @Override
    public void writeAll(List<? extends CsvWritable> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        File file = new File(statsFilePath);
        createParentDirectories(file);

        boolean shouldWriteHeader = !file.exists() || file.length() == 0;

        try (FileWriter writer = new FileWriter(statsFilePath, true)) {
            // Write header only for new/empty files
            if (shouldWriteHeader) {
                String header = items.getFirst().csvHeader();
                writer.append(header).append("\n");
            }

            // Write all data rows
            for (CsvWritable item: items) {
                writer.append(item.toCsvRow()).append("\n");
            }

            writer.flush();
        } catch (IOException ex) {
            throw new CsvWriteException(ex);
        }
    }

    /**
     * Creates parent directories if they don't exist
     */
    private void createParentDirectories(File file) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }
}
