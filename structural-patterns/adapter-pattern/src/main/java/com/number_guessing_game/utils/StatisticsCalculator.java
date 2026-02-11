package com.number_guessing_game.utils;

import com.number_guessing_game.domains.GameResults;
import com.number_guessing_game.domains.GameStatistics;
import com.number_guessing_game.enums.Competitors;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class StatisticsCalculator {
    public GameStatistics calculate(List<GameResults> results) {
        int totalGames = results.size();
        int userWins = (int) results.stream()
                .filter(r -> r.getWinner() == Competitors.USER)
                .count();
        int computerWins = (int) results.stream()
                .filter(r -> r.getWinner() == Competitors.COMPUTER)
                .count();
        double userWinRate = totalGames > 0 ? (double) userWins / totalGames : 0.0;
        int mostCommonWinningNumber = results.stream()
                .collect(Collectors.groupingBy(GameResults::getWinningNumber, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        LocalDateTime firstGame = results.getFirst().getDateTime();
        LocalDateTime lastGame = results.getLast().getDateTime();

        return GameStatistics.builder()
                .totalGames(totalGames)
                .userWins(userWins)
                .computerWins(computerWins)
                .userWinRate(userWinRate)
                .mostCommonWinningNumber(mostCommonWinningNumber)
                .firstGame(firstGame)
                .lastGame(lastGame)
                .build();
    }
}
