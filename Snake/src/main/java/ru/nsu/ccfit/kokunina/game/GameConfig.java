package ru.nsu.ccfit.kokunina.game;

import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

// wrapper to proto game config to easily add new parameters
public class GameConfig {
    private String playerName;
    private SnakesProto.GameConfig gameConfig;

    public GameConfig(String playerName, SnakesProto.GameConfig config) {
        this.playerName = playerName;
        gameConfig = config;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getWidth() {
        return gameConfig.getWidth();
    }
    public int getHeight() {
        return gameConfig.getHeight();
    }
    public int getFoodStatic() {
        return gameConfig.getFoodStatic();
    }
    public float getFoodPerPlayer() {
        return gameConfig.getFoodPerPlayer();
    }
    public int getStateDelayMs() {
        return gameConfig.getStateDelayMs();
    }
    public float getDeadFoodProb() {
        return gameConfig.getDeadFoodProb();
    }
    public int getPingDelayMs() {
        return gameConfig.getPingDelayMs();
    }
    public int getNodeTimeoutMs() {
        return gameConfig.getNodeTimeoutMs();
    }

}
