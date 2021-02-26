package com.idle.minimal;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Serializable} object containing values related to an instance of the game.
 */
public class GameData implements Serializable {
    private double lastUpdate;

    private double score = 0.0;
    private double delta = 0.0;

    private final List<Double> upgradeDeltas = new ArrayList<>();
    private final List<Double> upgradePrices = new ArrayList<>();
    private final List<Double> ownedUpgrades = new ArrayList<>();

    /**
     * {@link Constructor} for GameData. Sets {@link #lastUpdate} to the current time.
     */
    public GameData() {
        this.lastUpdate = System.currentTimeMillis() / 1000.0;
    }

    public double getLastUpdate() {
        return lastUpdate;
    }

    public double getScore() {
        return score;
    }

    public double getDelta() {
        return delta;
    }

    public void setLastUpdate(double lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public List<Double> getUpgradeDeltas() {
        return upgradeDeltas;
    }

    public List<Double> getUpgradePrices() {
        return upgradePrices;
    }

    public List<Double> getOwnedUpgrades() {
        return ownedUpgrades;
    }

    /**
     * Increases the score by a certain amount.
     *
     * @param amount the amount
     */
    public void addScore(double amount) {
        this.score += amount;
    }
}