package ru.nsu.ccfit.kokunina.controllers;

public class GameListItem {
    private final String gameIP;

    public GameListItem(String gameIP) {
        this.gameIP = gameIP;
    }

    public String getIp() {
        return gameIP;
    }
}
