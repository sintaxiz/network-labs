package ru.nsu.ccfit.kokunina.net;

import ru.nsu.ccfit.kokunina.game.SnakeDirection;

import java.net.SocketAddress;

public class NetworkNode {
    private long lastOnline;
    private SnakeDirection snakeDirection;
    private SocketAddress address;
    private String ip;
    private int port;
    private String playerName;
    private int id;

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

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getName() {
        return playerName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
