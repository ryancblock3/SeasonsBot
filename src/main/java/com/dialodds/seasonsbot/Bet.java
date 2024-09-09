package com.dialodds.seasonsbot;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Bet {
    private int id;

    @JsonProperty("user_id")
    private int userId;

    @JsonProperty("season_id")
    private int seasonId;

    @JsonProperty("game_id")
    private int gameId;

    @JsonProperty("bet_type")
    private String betType;

    private int amount;

    @JsonProperty("created_at")
    private Date createdAt;

    private String result;

    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    public Bet() {}

    public Bet(int id, int userId, int seasonId, int gameId, String betType, int amount, Date createdAt, String result, String homeTeam, String awayTeam) {
        this.id = id;
        this.userId = userId;
        this.seasonId = seasonId;
        this.gameId = gameId;
        this.betType = betType;
        this.amount = amount;
        this.createdAt = createdAt;
        this.result = result;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getBetType() {
        return betType;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }
}