package com.idle.minimal;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Timer;

/**
 * <h1>minimal.idle</h1>
 * <h2>A simple and minimalistic incremental game.</h2>
 *
 * @author Julian Lachniet
 * @version 1.8
 */
public class MinimalIdle extends AppCompatActivity {
    private Locale locale;

    private File saveFile;

    private GameData gameData = new GameData();

    private int saveCounter;

    /**
     * The code that is executed when the app opens.
     *
     * @param savedInstanceState the saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        saveFile = new File(getFilesDir(), "save");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }

        if (saveFile.exists()) {
            try {
                gameData = (GameData) new ObjectInputStream(new FileInputStream(saveFile))
                        .readObject();
                addUpgradeRows(gameData.getOwnedUpgrades().size());
            } catch (IOException | ClassNotFoundException e) {
                addUpgrade();
            }
        } else {
            addUpgrade();
        }

        saveCounter = 0;

        ScoreUpdaterTimer scoreUpdaterTimer = new ScoreUpdaterTimer(this);
        new Timer().scheduleAtFixedRate(scoreUpdaterTimer, 0, 50);
    }

    /**
     * Increments the score.
     *
     * @param view the view
     */
    @SuppressWarnings("unused")
    public void incrementScore(View view) {
        gameData.addScore(1.0);
    }

    /**
     * Checks whether the user can afford an upgrade, and purchases it if they have enough score.
     *
     * @param view the view
     */
    public void attemptPurchase(View view) {
        LinearLayout row = (LinearLayout) view.getParent().getParent();
        LinearLayout shop = (LinearLayout) row.getParent();

        int upgradeIndex = shop.indexOfChild(row) - 1;

        if (gameData.getScore() >= gameData.getUpgradePrices().get(upgradeIndex)
                && gameData.getOwnedUpgrades().get(upgradeIndex) < 100) {
            gameData.addScore(0 - gameData.getUpgradePrices().get(upgradeIndex));
            gameData.setDelta(gameData.getDelta() + gameData.getUpgradeDeltas().get(upgradeIndex));

            gameData.getOwnedUpgrades().set(
                    upgradeIndex,
                    gameData.getOwnedUpgrades().get(upgradeIndex) + 1);
            gameData.getUpgradePrices().set(
                    upgradeIndex,
                    Math.ceil(gameData.getUpgradePrices().get(upgradeIndex) * 1.25));

            updateShopNumbers();
        }
    }

    /**
     * Updates the current score and adds a new upgrade if necessary.
     */
    public void updateScore() {
        double currentTime = System.currentTimeMillis() / 1000.0;

        double scoreEarned = gameData.getDelta() * (currentTime - gameData.getLastUpdate());

        if (gameData.getScore() + scoreEarned < Double.MAX_VALUE) {
            gameData.addScore(scoreEarned);
        }

        gameData.setLastUpdate(currentTime);

        updateAppBarNumbers();

        if (Collections.max(gameData.getUpgradePrices()) < gameData.getScore() * 100
                && gameData.getOwnedUpgrades().size() < 221) {
            addUpgrade();
        }

        if (saveCounter++ > 50) {
            saveCounter = 0;

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

                objectOutputStream.writeObject(gameData);
                objectOutputStream.close();

                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds an upgrade to the {@link GameData} and calls {@link #addUpgradeRows}.
     *
     * @see GameData
     * @see #addUpgradeRows(int)
     */
    private void addUpgrade() {
        int index = gameData.getOwnedUpgrades().size();

        gameData.getUpgradeDeltas().add(Math.pow(10, index));
        gameData.getUpgradePrices().add(Math.pow(25.0, index));
        gameData.getOwnedUpgrades().add(0.0);

        addUpgradeRows(1);
    }

    /**
     * Adds some number of upgrade rows to the layout.
     *
     * @param count the number of rows
     */
    private void addUpgradeRows(int count) {
        for (int i = 0; i < count; i++) {
            View.inflate(this, R.layout.shop_row, findViewById(R.id.shop));
        }

        updateShopNumbers();
    }

    /**
     * Updates the numbers in the app bar.
     */
    private void updateAppBarNumbers() {
        ((TextView) findViewById(R.id.score)).setText(
                String.format(
                        locale,
                        "%s: %s",
                        getString(R.string.score),
                        formatScientific(gameData.getScore())));
        ((TextView) findViewById(R.id.delta)).setText(
                String.format(locale,
                        "%s: %s",
                        getString(R.string.delta),
                        formatScientific(gameData.getDelta())));
    }

    /**
     * Updates the numbers in the shop.
     */
    private void updateShopNumbers() {
        LinearLayout shop = findViewById(R.id.shop);

        for (int i = 0; i < shop.getChildCount() - 1; i++) {
            LinearLayout row = ((LinearLayout) shop.getChildAt(i + 1));

            String upgradeLetters = "abcdefghijklmnopqrstuvwxyz"
                    + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "αβγδεζηθικλμνξοπρστυφχψω";
            String upgradeName;
            if (i < upgradeLetters.length()) {
                upgradeName = Character.toString(upgradeLetters.charAt(i));
            } else {
                upgradeName = Integer.toString(i + 1);
            }

            if (gameData.getOwnedUpgrades().get(i) < 100) {
                ((TextView) row.getChildAt(0))
                        .setText(String.format(locale,
                                "%s %s (Δ %s)%n%s %s",
                                getString(R.string.upgrade),
                                upgradeName,
                                formatScientific(gameData.getUpgradeDeltas().get(i)),
                                getString(R.string.cost),
                                formatScientific(gameData.getUpgradePrices().get(i))));
            } else {
                ((TextView) row.getChildAt(0))
                        .setText(String.format(locale,
                                "%s %s (Δ %s)%n%s",
                                getString(R.string.upgrade),
                                upgradeName,
                                formatScientific(gameData.getUpgradeDeltas().get(i)),
                                getString(R.string.all_purchased)));
            }

            ((TextView) ((LinearLayout) row.getChildAt(1))
                    .getChildAt(0))
                    .setText(String.valueOf(gameData.getOwnedUpgrades().get(i).intValue()));
        }
    }

    /**
     * Converts a double to a string, and formats it using scientific notation if it's above or
     * equal to 10^6.
     *
     * @param number the number
     * @return the formatted number
     */
    private String formatScientific(double number) {
        if (number < Math.pow(10, 6)) {
            return String.valueOf((int) number);
        }

        return new DecimalFormat("0.00E0").format(number).toLowerCase();
    }
}