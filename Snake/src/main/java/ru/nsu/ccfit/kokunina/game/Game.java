package ru.nsu.ccfit.kokunina.game;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.multicast.MulticastSender;

import java.net.UnknownHostException;
import java.util.Random;

public class Game {
    private final static Logger log = LoggerFactory.getLogger(Game.class);

    private final StringProperty foodCount;
    private final StringProperty masterName;
    private final Cell[][] cells;
    private final Snake masterSnake;

    private final int COLUMN_COUNT = 30;
    private final int ROWS_COUNT = 40;

    public Game() {
        foodCount = new SimpleStringProperty("300");
        masterName = new SimpleStringProperty("Danil");
        cells = new Cell[COLUMN_COUNT][ROWS_COUNT];
        for (int i = 0; i < COLUMN_COUNT; i++) {
            for (int j = 0; j < ROWS_COUNT; j++) {
                cells[i][j] = new Cell();
            }
        }
        masterSnake = new Snake(cells, new Coordinates(20, 10), SnakeDirection.LEFT);
        try {
            Thread sender = new Thread(new MulticastSender());
            sender.start();
        } catch (UnknownHostException e) {
            System.out.println("Can not start game");
            e.printStackTrace();
        }
        lastDirection = masterSnake.getDirection();
    }

    private SnakeDirection lastDirection;

    private void updateSnakePosition() {
        int newHeadX = masterSnake.getHeadCoord().getX();
        int newHeadY = masterSnake.getHeadCoord().getY();
        log.debug("direction = " + masterSnake.getDirection());

        SnakeDirection newDirection = masterSnake.getDirection();
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
        masterSnake.setPosition(new Coordinates(newHeadX, newHeadY));
    }

    private void addNewFood(Coordinates foodCoord) {
        int x = foodCoord.getX();
        int y = foodCoord.getY();
        if (cells[x][y].getState() == CellState.EMPTY) {
            cells[x][y].setState(CellState.FOOD);
        }
    }

    private void updateFood() {
        int randomX = new Random().nextInt(COLUMN_COUNT);
        int randomY = new Random().nextInt(ROWS_COUNT);
        addNewFood(new Coordinates(randomX,randomY));
    }

    public void update() {
        updateSnakePosition();
        updateFood();
    }

    public void setMasterSnakeDirection(SnakeDirection direction) {
        masterSnake.setDirection(direction);
    }

    public Pair<Integer, Integer> getGameFieldSize() {
        return new Pair<>(COLUMN_COUNT, ROWS_COUNT);
    }

    public StringProperty masterNameProperty() {
        return masterName;
    }

    public StringProperty foodCountProperty() {
        return foodCount;
    }

    public ObservableValue<CellState> CellStateProperty(int x, int y) {
        return cells[x][y].getCellStateProperty();
    }

}
