package ru.nsu.ccfit.kokunina.game;

import java.util.LinkedList;

public class Snake {

    private SnakeDirection direction;
    private final LinkedList<Coordinates> coordinates;
    private final Cell[][] cells;

    public Snake(Cell[][] cells,Coordinates initCoord, SnakeDirection initDirect) {
        coordinates = new LinkedList<>();
        coordinates.add(initCoord);
        direction = initDirect;
        this.cells = cells;
        switch (direction) {
            case UP -> {
                coordinates.add(new Coordinates(initCoord.getX(), initCoord.getY() - 1));
                coordinates.add(new Coordinates(initCoord.getX(), initCoord.getY() - 2));
            }
            case DOWN -> {
                coordinates.add(new Coordinates(initCoord.getX(), initCoord.getY() + 1));
                coordinates.add(new Coordinates(initCoord.getX(), initCoord.getY() + 2));
            }
            case LEFT -> {
                coordinates.add(new Coordinates(initCoord.getX() + 1, initCoord.getY()));
                coordinates.add(new Coordinates(initCoord.getX() + 2, initCoord.getY()));
            }
            case RIGHT -> {
                coordinates.add(new Coordinates(initCoord.getX() - 1, initCoord.getY()));
                coordinates.add(new Coordinates(initCoord.getX() - 2, initCoord.getY()));
            }
        }
        for (Coordinates coord : coordinates) {
            cells[coord.getX()][coord.getY()].setState(CellState.SNAKE);
        }

    }

    public SnakeDirection getDirection() {
        return direction;
    }

    public void setDirection(SnakeDirection direction) {
        this.direction = direction;
    }

    public int getX() {
        return coordinates.getFirst().getX();
    }

    public int getY() {
        return coordinates.getFirst().getY();
    }

    public void setPosition(Coordinates position) {
        int newX = position.getX();
        int newY = position.getY();
        switch (cells[newX][newY].getState()) {
            case FOOD -> {
                coordinates.addFirst(position);
                cells[position.getX()][position.getY()].setState(CellState.SNAKE);
            }
            case EMPTY -> {
                Coordinates tailCoord = getTailCoord();
                cells[tailCoord.getX()][tailCoord.getY()].setState(CellState.EMPTY);
                cells[position.getX()][position.getY()].setState(CellState.SNAKE);
                coordinates.addFirst(position);
                coordinates.removeLast();
            }
            case SNAKE -> {
                System.exit(0); // todo gameover
            }
        }

    }

    public Coordinates getTailCoord() {
        return coordinates.getLast();
    }

    public Coordinates getHeadCoord() {
        return coordinates.getFirst();
    }

    public LinkedList<Coordinates> getCoord() {
        return coordinates;
    }
}
