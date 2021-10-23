package ru.nsu.ccfit.kokunina.game;

import ru.nsu.ccfit.kokunina.game.Cell;

public class Field {
    final private Cell[][] field;

    public Field(int width, int height) {
        field = new Cell[width][height];
    }

    public int getWidth() {
        return field.length;
    }

    public int getHeight() {
        return field[0].length;
    }

    public Cell getCell(int x, int y) {
        return field[x][y];
    }
}
