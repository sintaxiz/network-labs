package ru.nsu.ccfit.kokunina.game;

public class Field {
    final private Cell[][] cells;
    private final int width;
    private final int height;


    public Field(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new Cell[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell getCell(int row, int column) {
        return cells[row][column];
    }
}
