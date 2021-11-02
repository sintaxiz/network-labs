package ru.nsu.ccfit.kokunina.game;

import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.net.MasterNetworkService;
import ru.nsu.ccfit.kokunina.net.NetworkService;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

public class Game {
    private final static Logger log = LoggerFactory.getLogger(Game.class);

    // Game objects
    private Snake masterSnake;
    private final Field gameField;
    private final ArrayList<Snake> snakes;
    private final FoodController foodController;

    // Network objects
    private final NetworkService networkService;

    public Game(GameConfig config, NetworkService networkService) throws IOException {
        this.networkService = networkService;
        snakes = new ArrayList<>();
        gameField = new Field(config.getWidth(), config.getHeight());
        foodController = new FoodController(gameField, config.getFoodStatic(), config.getFoodPerPlayer());
    }

    /**
     * Starts network service and add snake to the field
     * Use in bundle with {@link ru.nsu.ccfit.kokunina.game.Game#stop() stop} method to free resources
     */
    public void start() {
        masterSnake = new Snake(gameField, new Coordinates(11, 11), SnakeDirection.LEFT, this);
        snakes.add(new Snake(gameField, new Coordinates(10, 10), SnakeDirection.LEFT, this));
    }

    public void update() {
        updateNetworkData();

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
        foodController.update(snakes.size());

        notifyNetwork();
    }

    private void notifyNetwork() {
        //masterNetworkService.sendGameState(gameState);
    }

    private void updateNetworkData() {
       /* ArrayList<Player> players = masterNetworkService.getAllPlayers();
        for (Player player : players) {
            if (!player.isOnline()) {
                //snakes[player]
            }
        }*/
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
}
