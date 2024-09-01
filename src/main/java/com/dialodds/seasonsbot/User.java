package com.dialodds.seasonsbot;

public class User {
    private int id;
    private String discordId;
    private String username;
    private int coins;

    // Constructors
    public User() {}

    public User(int id, String discordId, String username, int coins) {
        this.id = id;
        this.discordId = discordId;
        this.username = username;
        this.coins = coins;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}