package ru.nsu.ccfit.kokunina.controllers;

import ru.nsu.ccfit.kokunina.game.GameConfig;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.net.SocketAddress;

public class GameListItem {
    private final SocketAddress gameAddress;
    private final boolean canJoin;
    private final GameConfig gameConfig;

    public GameListItem(SocketAddress gameAddress, SnakesProto.GameConfig config,
                        SnakesProto.GamePlayers players, boolean canJoin) {
        this.gameAddress = gameAddress;
        this.canJoin = canJoin;
        this.gameConfig = new GameConfig("name", config);
    }

    public SocketAddress getGameAddress() {
        return gameAddress;
    }

    public boolean isCanJoin() {
        return canJoin;
    }

    public GameConfig getConfig() {
        return gameConfig;
    }
}
