package com.dialodds.seasonsbot;

import java.util.Date;

public class Season {
    private int id;
    private int startWeek;
    private int endWeek;
    private int initialCoins;
    private Date createdAt;
    private boolean isActive;

    // Constructors
    public Season() {}

    public Season(int id, int startWeek, int endWeek, int initialCoins, Date createdAt, boolean isActive) {
        this.id = id;
        this.startWeek = startWeek;
        this.endWeek = endWeek;
        this.initialCoins = initialCoins;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(int startWeek) {
        this.startWeek = startWeek;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(int endWeek) {
        this.endWeek = endWeek;
    }

    public int getInitialCoins() {
        return initialCoins;
    }

    public void setInitialCoins(int initialCoins) {
        this.initialCoins = initialCoins;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}