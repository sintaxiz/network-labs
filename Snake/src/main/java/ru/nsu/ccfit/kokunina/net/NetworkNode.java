package ru.nsu.ccfit.kokunina.net;

import ru.nsu.ccfit.kokunina.game.SnakeDirection;

public class NetworkNode {
    private long lastOnline;
    private SnakeDirection snakeDirection;

    public NetworkNode() {
        lastOnline = System.currentTimeMillis();
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public SnakeDirection getSnakeDirection() {
        return snakeDirection;
    }

    public void setSnakeDirection(SnakeDirection snakeDirection) {
        this.snakeDirection = snakeDirection;
    }
}
