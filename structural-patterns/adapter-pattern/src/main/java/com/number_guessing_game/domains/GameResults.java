package com.number_guessing_game.domains;

import com.number_guessing_game.enums.Competitors;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class GameResults implements CsvWritable {
    private static final DateTimeFormatter CSV_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime dateTime;
    private Competitors winner;
    private int winningNumber;

    @Override
    public String csvHeader() {
        return "DATE_TIME,WINNER,WINNING_NUM";
    }

    @Override
    public String toCsvRow() {
        return String.format("%s,%s,%d",
                dateTime.format(CSV_DATE_FORMAT),
                winner.name(),
                winningNumber
        );
    }
}
