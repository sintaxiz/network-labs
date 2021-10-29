package ru.nsu.ccfit.kokunina.game;

import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.net.MasterNetworkService;
import ru.nsu.ccfit.kokunina.net.multicast.MulticastSender;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

public class Game {
    private final static Logger log = LoggerFactory.getLogger(Game.class);

    // Game objects
    private Field gameField;
    private final Snake masterSnake;
    private final ArrayList<Snake> snakes;
    private final FoodController foodController;

    // Network objects
    private final MasterNetworkService masterNetworkService;

    public Game(GameConfig config) throws SocketException {

        masterNetworkService = new MasterNetworkService();

        snakes = new ArrayList<>();
        gameField = new Field(config.getWidth(), config.getHeight());
        foodController = new FoodController(gameField, config.getFoodStatic(), config.getFoodPerPlayer());
        masterSnake = new Snake(gameField, new Coordinates(11, 11), SnakeDirection.LEFT, this);
        snakes.add(new Snake(gameField, new Coordinates(10, 10), SnakeDirection.LEFT, this));

        // network connection. i think it should be another func and not here
        try {
            Thread sender = new Thread(new MulticastSender());
            sender.start();
        } catch (UnknownHostException e) {
            System.out.println("Can not start game");
            e.printStackTrace();
        }
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
