package ru.nsu.ccfit.kokunina.controllers;

import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.game.CellState;
import ru.nsu.ccfit.kokunina.game.GameConfig;
import ru.nsu.ccfit.kokunina.game.SnakeDirection;
import ru.nsu.ccfit.kokunina.net.NetworkService;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NormalGameController implements Initializable, GameController {
    private final Logger log = LoggerFactory.getLogger(MasterGameController.class);

    private final String CELL_COLOR = "#d3d3cb";
    private final String SNAKE_COLOR = "#9f2b00";
    private final String FOOD_COLOR = "#ada7a7";

    public ListView<SnakesProto.GamePlayer> playerList;
    public Text masterName;
    public Text fieldSize;
    public Text foodCount;
    public GridPane gameField;

    private GameConfig config;
    private NetworkService networkService;
    private ArrayList<ObjectProperty<CellState>> cellStates;

    int snakeId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void handleKeyPressed(KeyEvent keyEvent) throws IOException {
        switch (keyEvent.getCode()) {
            case W -> networkService.setPlayerSnakeDirection(snakeId, SnakeDirection.UP);
            case S -> networkService.setPlayerSnakeDirection(snakeId, SnakeDirection.DOWN);
            case A -> networkService.setPlayerSnakeDirection(snakeId, SnakeDirection.LEFT);
            case D -> networkService.setPlayerSnakeDirection(snakeId, SnakeDirection.RIGHT);
        }
    }

    public void handleExitGameButton(ActionEvent actionEvent) throws IOException {
        // go to main screen
        networkService.interrupt();
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        Stage currentStage = (Stage) gameField.getScene().getWindow();
        currentStage.setScene(new Scene(mainMenu));
    }

    private void initField() {
        gameField.setBackground(new Background(new BackgroundFill(Color.valueOf(CELL_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        int rows = config.getHeight();
        int columns = config.getWidth();
        final int CELL_SIZE = 400 / rows;
        fieldSize.setText(columns + "x" + rows);
        cellStates = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE, Color.valueOf(CELL_COLOR));
                gameField.add(cell, column, row);
                ObjectProperty<CellState> state = new SimpleObjectProperty<>(CellState.EMPTY);
                state.addListener((observable, oldState, newState) -> {
                    switch (newState) {
                        case SNAKE -> cell.setFill(Color.valueOf(SNAKE_COLOR));
                        case FOOD -> cell.setFill(Color.valueOf(FOOD_COLOR));
                        case EMPTY -> cell.setFill(Color.valueOf(CELL_COLOR));
                    }
                });
                //state.bind(networkService.gameFieldCellStateProperty(row, column));
                cellStates.add(state);
            }
        }
    }

    public void startGame(GameConfig gameConfig, NetworkService networkService, int playerId) {
        this.networkService = networkService;
        config = gameConfig;
        snakeId = playerId;
        masterName.setText(config.getPlayerName());
        foodCount.setText(config.getFoodStatic() + "+" + config.getFoodPerPlayer());
        networkService.start();
//        playerList.itemsProperty().bind(networkService.playersProperty());
        playerList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<SnakesProto.GamePlayer> call(ListView<SnakesProto.GamePlayer> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(SnakesProto.GamePlayer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName() + ": " + item.getScore());
                        } else {
                            setText("");
                        }
                    }
                };
            }
        });
        initField();

    }

    private void updatePlayerList(SnakesProto.GamePlayers players) {
        List<SnakesProto.GamePlayer> playersList = players.getPlayersList();
        for (SnakesProto.GamePlayer player : playersList) {
            if (playerList.getItems().contains(player)) {
                continue;
            }
            playerList.getItems().add(player);
        }
    }

    private void freeField() {
        int width = config.getWidth();
        int height = config.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cellStates.get(i * height + j).setValue(CellState.EMPTY);
            }
        }
    }

    private void drawSnakes(List<SnakesProto.GameState.Snake> snakesList) {
        for (SnakesProto.GameState.Snake snake : snakesList) {
            for (SnakesProto.GameState.Coord point : snake.getPointsList()) {
                cellStates.get(point.getY() * config.getWidth() + point.getX()).setValue(CellState.SNAKE);
            }
        }
    }

    private void drawFood(List<SnakesProto.GameState.Coord> foodsList) {
        for (SnakesProto.GameState.Coord food : foodsList) {
            cellStates.get(food.getY() * config.getWidth() + food.getX()).setValue(CellState.FOOD);
        }
    }

    @Override
    public void setGameState(SnakesProto.GameState state) {
        freeField();
        drawFood(state.getFoodsList());
        drawSnakes(state.getSnakesList());
        updatePlayerList(state.getPlayers());
    }

    @Override
    public void addPlayer(int uid) {
    }

    @Override
    public void setSnakeDirection(int snakeId, SnakeDirection direction) {

    }

    @Override
    public void normalPlayerToViewer(int senderId) {

    }

    @Override
    public void deputyToMaster() {

    }

}
