package ru.nsu.ccfit.kokunina.game;

public class Coordinates {
    @Override
    public String toString() {
        return "(" + X + ", " + Y + ")";
    }

    public Coordinates(int x, int y) {
        X = x;
        Y = y;
    }

    private int X;
    private int Y;

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }
}
