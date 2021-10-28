package ru.nsu.ccfit.kokunina.game;

import java.util.LinkedList;

public class Snake {

    private SnakeDirection direction;
    private final LinkedList<Coordinates> coordinates;
    private final Field field;
    private boolean isDead = false;
    private final Game game;

    public Snake(Field field, Coordinates initCoord, SnakeDirection initDirect, Game game) {
        this.game = game;
        coordinates = new LinkedList<>();
        coordinates.add(initCoord);
        direction = initDirect;
        this.field = field;
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
            field.getCell(coord.getY(), coord.getX()).setState(CellState.SNAKE);
        }
        lastDirection = initDirect;
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
        switch (field.getCell(newY, newX).getState()) {
            case FOOD -> {
                coordinates.addFirst(position);
                game.eatFood(position);
            }
            case EMPTY -> {
                Coordinates tailCoord = getTailCoord();
                field.getCell(tailCoord.getY(), tailCoord.getX()).setState(CellState.EMPTY);
                field.getCell(position.getY(), position.getX()).setState(CellState.SNAKE);
                coordinates.addFirst(position);
                coordinates.removeLast();
            }
            case SNAKE -> {
                deleteFromField();
                isDead = true;
            }
        }
    }

    private void deleteFromField() {
        for (Coordinates coord : coordinates) {
            field.getCell(coord.getY(), coord.getX()).setState(CellState.EMPTY);
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

    private SnakeDirection lastDirection;

    public void updatePosition() {
        if (isDead) {
            return;
        }
        int ROWS_COUNT = field.getHeight();
        int COLUMN_COUNT = field.getWidth();
        int newHeadX = getHeadCoord().getX();
        int newHeadY = getHeadCoord().getY();

        SnakeDirection newDirection = getDirection();
        // check if snake tries to reverse
        newDirection = (lastDirection == SnakeDirection.UP && newDirection == SnakeDirection.DOWN) ?
                lastDirection : newDirection;
        newDirection = (lastDirection == SnakeDirection.DOWN && newDirection == SnakeDirection.UP) ?
                lastDirection : newDirection;
        newDirection = (lastDirection == SnakeDirection.LEFT && newDirection == SnakeDirection.RIGHT) ?
                lastDirection : newDirection;
        newDirection = (lastDirection == SnakeDirection.RIGHT && newDirection == SnakeDirection.LEFT) ?
                lastDirection : newDirection;
        switch (newDirection) {
            case UP -> {
                newHeadY = (newHeadY - 1 + ROWS_COUNT) % ROWS_COUNT;
            }
            case DOWN -> {
                newHeadY = (newHeadY + 1 + ROWS_COUNT) % ROWS_COUNT;
            }
            case RIGHT -> {
                newHeadX = (newHeadX + 1 + COLUMN_COUNT) % COLUMN_COUNT;
            }
            case LEFT -> {
                newHeadX = (newHeadX - 1 + COLUMN_COUNT) % COLUMN_COUNT;
            }
        }
        lastDirection = newDirection;
        setPosition(new Coordinates(newHeadX, newHeadY));
    }

    public boolean isDead() {
        return isDead;
    }
}

