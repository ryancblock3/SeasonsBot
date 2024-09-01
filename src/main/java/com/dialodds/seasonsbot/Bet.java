package com.dialodds.seasonsbot;

import java.util.Date;

public class Bet {
    private int id;
    private int userId;
    private int seasonId;
    private int gameId;
    private String betType;
    private int amount;
    private Date createdAt;
    private String status;
    private String homeTeam;
    private String awayTeam;

    // Constructors
    public Bet() {}

    public Bet(int id, int userId, int seasonId, int gameId, String betType, int amount, Date createdAt, String status, String homeTeam, String awayTeam) {
        this.id = id;
        this.userId = userId;
        this.seasonId = seasonId;
        this.gameId = gameId;
        this.betType = betType;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = status;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    // Getters and setters
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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