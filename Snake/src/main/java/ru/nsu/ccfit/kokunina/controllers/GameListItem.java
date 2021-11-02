package ru.nsu.ccfit.kokunina.controllers;

import java.net.SocketAddress;

public class GameListItem {
    private final SocketAddress gameAddress;

    public GameListItem(SocketAddress gameAddress) {
        this.gameAddress = gameAddress;
    }

    public SocketAddress getGameAddress() {
        return gameAddress;
    }
}
