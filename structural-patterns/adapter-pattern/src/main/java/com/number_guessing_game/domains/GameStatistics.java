package com.number_guessing_game.domains;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameStatistics {
    int totalGames;
    int userWins;
    int computerWins;
    double userWinRate;
    int mostCommonWinningNumber;
    LocalDateTime firstGame;
    LocalDateTime lastGame;
}
