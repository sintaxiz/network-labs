package ru.nsu.ccfit.kokunina.game;

public class Field {
    final private Cell[][] field;
    final private int width;
    final private int height;

    public Field(int width, int height) {
        field = new Cell[width][height];
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell getCell(int x, int y) {
        return field[x][y];
    }
}
