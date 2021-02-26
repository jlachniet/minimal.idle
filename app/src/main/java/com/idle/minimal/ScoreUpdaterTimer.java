package com.idle.minimal;

import java.util.TimerTask;

/**
 * A {@link TimerTask} that tells the game to update the score with {@link
 * MinimalIdle#updateScore()}.
 */
public class ScoreUpdaterTimer extends TimerTask {
    final MinimalIdle context;

    public ScoreUpdaterTimer(MinimalIdle context) {
        this.context = context;
    }

    /**
     * Tells the game to update the score.
     */
    @Override
    public void run() {
        context.runOnUiThread(context::updateScore);
    }
}
