package ru.nsu.ccfit.kokunina.game;

import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

public class GameConfig {
    SnakesProto.GameConfig gameConfig;

    public GameConfig(SnakesProto.GameConfig config) {
        gameConfig = config;
    }

    public int getHeight() {
        return gameConfig.getHeight();
    }
    public int getWidth() {
        return gameConfig.getWidth();
    }
}
