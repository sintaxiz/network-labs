package ru.nsu.ccfit.kokunina.game;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.multicast.MulticastSender;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Game {
    private final static Logger log = LoggerFactory.getLogger(Game.class);

    private final StringProperty foodCount;
    private final StringProperty masterName;
    private final Cell[][] cells;
    private final Snake masterSnake;
    private final ArrayList<Snake> snakes;

    private final int COLUMN_COUNT;
    private final int ROWS_COUNT;

    public Game(GameConfig config) {
        foodCount = new SimpleStringProperty("300");
        masterName = new SimpleStringProperty("Danil");
        snakes = new ArrayList<>();
        COLUMN_COUNT = config.getWidth();
        ROWS_COUNT = config.getHeight();
        cells = new Cell[COLUMN_COUNT][ROWS_COUNT];
        for (int i = 0; i < COLUMN_COUNT; i++) {
            for (int j = 0; j < ROWS_COUNT; j++) {
                cells[i][j] = new Cell();
            }
        }
        masterSnake = new Snake(cells, new Coordinates(0, 0), SnakeDirection.LEFT);
        try {
            Thread sender = new Thread(new MulticastSender());
            sender.start();
        } catch (UnknownHostException e) {
            System.out.println("Can not start game");
            e.printStackTrace();
        }

        snakes.add(new Snake(cells, new Coordinates(10, 10), SnakeDirection.LEFT));
        snakes.add(new Snake(cells, new Coordinates(10, 11), SnakeDirection.LEFT));
        snakes.add(new Snake(cells, new Coordinates(10, 12), SnakeDirection.LEFT));

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
        if (!masterSnake.isDead()) {
            masterSnake.updatePosition();
        }
        Iterator<Snake> iter = snakes.iterator();
        while (iter.hasNext()) {
            Snake snake = iter.next();
            snake.updatePosition();
            if (snake.isDead()) {
                iter.remove();
            }
        }
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
