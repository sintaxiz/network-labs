package ru.nsu.ccfit.kokunina.game;

import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.net.NetworkService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Game {
    private final static Logger log = LoggerFactory.getLogger(Game.class);

    // Game objects
    private Snake masterSnake;
    private final Field gameField;
    private final HashMap<Integer, Snake> snakes;
    private final FoodController foodController;

    // Network objects
    private final NetworkService networkService;

    public Game(GameConfig config, NetworkService networkService) throws IOException {
        this.networkService = networkService;
        snakes = new HashMap<>();
        gameField = new Field(config.getWidth(), config.getHeight());
        foodController = new FoodController(gameField, config.getFoodStatic(), config.getFoodPerPlayer());
    }


    public void start() {
        masterSnake = new Snake(gameField, new Coordinates(11, 11), SnakeDirection.LEFT, this, 1);
        snakes.put(0, masterSnake);
    }

    public void update() throws IOException {
        Iterator<Snake> iter = snakes.values().iterator();
        while (iter.hasNext()) {
            Snake snake = iter.next();
            snake.updatePosition();
            if (snake.isDead()) {
                iter.remove();
            }
        }
        foodController.update(snakes.size());
        networkService.notifyNetwork(foodController.getFoodsCoord(), new ArrayList<>(snakes.values()));
    }

    /**
     * Tries to find empty space on the game field and creates new snake with random direction
     */
    public void addSnake(int uid) {
        snakes.put(uid, new Snake(gameField, new Coordinates(10, 10), SnakeDirection.LEFT, this, uid));
    }

    public void setMasterSnakeDirection(SnakeDirection direction) {
        masterSnake.setDirection(direction);
    }

    public ObservableValue<CellState> gameFieldCellStateProperty(int row, int column) {
        return gameField.getCell(row, column).getCellStateProperty();
    }

    public void eatFood(Coordinates position) {
        foodController.eatFood(position);
    }


    public void setSnakeDirection(int snakeId, SnakeDirection direction) {
        log.debug("set snake #" + snakeId + " direction = " + direction);
        snakes.get(snakeId).setDirection(direction);
    }
}
