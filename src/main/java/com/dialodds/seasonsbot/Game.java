package com.dialodds.seasonsbot;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Game {
    private int id;

    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    @JsonProperty("commence_time")
    private Instant commenceTime;

    @JsonProperty("home_odds")
    private double homeOdds;

    @JsonProperty("away_odds")
    private double awayOdds;

    // Constructors
    public Game() {
    }

    public Game(int id, String homeTeam, String awayTeam, Instant commenceTime, double homeOdds, double awayOdds) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.commenceTime = commenceTime;
        this.homeOdds = homeOdds;
        this.awayOdds = awayOdds;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Instant getCommenceTime() {
        return commenceTime;
    }

    public void setCommenceTime(Instant commenceTime) {
        this.commenceTime = commenceTime;
    }

    public double getHomeOdds() {
        return homeOdds;
    }

    public void setHomeOdds(double homeOdds) {
        this.homeOdds = homeOdds;
    }

    public double getAwayOdds() {
        return awayOdds;
    }

    public void setAwayOdds(double awayOdds) {
        this.awayOdds = awayOdds;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", homeTeam='" + homeTeam + '\'' +
                ", awayTeam='" + awayTeam + '\'' +
                ", commenceTime=" + commenceTime +
                ", homeOdds=" + homeOdds +
                ", awayOdds=" + awayOdds +
                '}';
    }
}