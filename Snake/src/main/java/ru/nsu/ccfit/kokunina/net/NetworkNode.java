package ru.nsu.ccfit.kokunina.net;

import ru.nsu.ccfit.kokunina.game.SnakeDirection;

import java.net.SocketAddress;

public class NetworkNode {
    private long lastOnline;
    private SnakeDirection snakeDirection;
    private SocketAddress address;

    public NetworkNode(SocketAddress address) {
        lastOnline = System.currentTimeMillis();
        this.address = address;
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

    public SocketAddress getAddress() {
        return address;
    }
}
