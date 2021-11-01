package ru.nsu.ccfit.kokunina.controllers;

import javafx.animation.KeyFrame;
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
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.game.CellState;
import ru.nsu.ccfit.kokunina.game.Game;
import ru.nsu.ccfit.kokunina.game.GameConfig;
import ru.nsu.ccfit.kokunina.game.SnakeDirection;
import ru.nsu.ccfit.kokunina.net.multicast.MulticastSender;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    private final Logger log = LoggerFactory.getLogger(GameController.class);

    private final String PLAYER_NAME = "Danil";
    private final String CELL_COLOR = "#d3d3cb";
    private final String SNAKE_COLOR = "#9f2b00";
    private final String FOOD_COLOR = "#ada7a7";
    private final double UPDATE_TIME_SEC = 0.1;

    public ListView<Text> playerList;
    public Text masterName;
    public Text fieldSize;
    public Text foodCount;
    public GridPane gameField;

    private Game game;
    private Thread networkThread;
    private Timeline timeline;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        GameConfig gameConfig = readConfig();
        masterName.setText(gameConfig.getPlayerName());
        foodCount.setText(gameConfig.getFoodStatic() + "+" + gameConfig.getFoodPerPlayer());

        // connect model & view
        try {
            game = new Game(gameConfig);
            game.start();
        } catch (SocketException e) {
            throw new RuntimeException("Can not start game", e);
        }
        gameField.setBackground(new Background(new BackgroundFill(Color.valueOf(CELL_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        int rows = gameConfig.getHeight();
        int columns = gameConfig.getWidth();
        final int CELL_SIZE = 400 / rows;
        fieldSize.setText(columns + "x" + rows);
        ArrayList<ObjectProperty<CellState>> states = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE, Color.valueOf(CELL_COLOR)) ;
                gameField.add(cell, column, row);
                ObjectProperty<CellState> state = new SimpleObjectProperty<>(CellState.EMPTY);
                state.addListener((observable, oldState, newState) -> {
                    switch (newState) {
                        case SNAKE -> cell.setFill(Color.valueOf(SNAKE_COLOR));
                        case FOOD -> cell.setFill(Color.valueOf(FOOD_COLOR));
                        case EMPTY -> cell.setFill(Color.valueOf(CELL_COLOR));
                    }
                });
                state.bind(game.gameFieldCellStateProperty(row, column));
                states.add(state);
            }
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(UPDATE_TIME_SEC), e -> {
            game.update();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        try {
            networkThread = new Thread(new MulticastSender());
            networkThread.start();
        } catch (UnknownHostException e) {
            System.out.println("Can not start game");
            e.printStackTrace();
        }
    }

    private GameConfig readConfig() {
        SnakesProto.GameConfig defaultConfig = SnakesProto.GameConfig.newBuilder().setHeight(30).build();
        return new GameConfig(PLAYER_NAME, defaultConfig);
    }

    @FXML
    private void handleKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case W -> game.setMasterSnakeDirection(SnakeDirection.UP);
            case S -> game.setMasterSnakeDirection(SnakeDirection.DOWN);
            case A -> game.setMasterSnakeDirection(SnakeDirection.LEFT);
            case D -> game.setMasterSnakeDirection(SnakeDirection.RIGHT);
        }
    }

    public void handleExitGameButton(ActionEvent actionEvent) throws IOException {
        // go to main screen
        timeline.stop();
        networkThread.interrupt();
        game.stop();
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        Stage currentStage = (Stage) gameField.getScene().getWindow();
        currentStage.setScene(new Scene(mainMenu));
    }
}
