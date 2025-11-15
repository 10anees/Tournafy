package com.example.tournafy.domain.models.statistics;
import com.example.tournafy.domain.models.statistics.StatType;

/**
 * A Data Transfer Object (DTO) used to hold aggregated statistics
 * for the "Top Performers" lists in Tournaments and Series.
 */
public class AggregatedStat {

    private String playerId;
    private String playerName;
    private StatType statType; // e.g., RUNS, GOALS 
    private double statisticValue; // Using double to accommodate NRR, averages, or simple counts

    // Constructors
    public AggregatedStat() {
    }

    public AggregatedStat(String playerId, String playerName, StatType statType, double statisticValue) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.statType = statType;
        this.statisticValue = statisticValue;
    }

    // Getters
    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public StatType getStatType() {
        return statType;
    }

    public double getStatisticValue() {
        return statisticValue;
    }

    // Setters
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setStatType(StatType statType) {
        this.statType = statType;
    }

    public void setStatisticValue(double statisticValue) {
        this.statisticValue = statisticValue;
    }
}