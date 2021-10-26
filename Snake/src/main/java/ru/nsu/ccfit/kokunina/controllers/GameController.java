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
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.game.CellState;
import ru.nsu.ccfit.kokunina.game.Game;
import ru.nsu.ccfit.kokunina.game.SnakeDirection;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    private final Logger log = LoggerFactory.getLogger(GameController.class);

    public ListView<Text> playerList;
    public Text masterName;
    public Text fieldSize;
    public Text foodCount;
    public GridPane gameField;

    private Game game;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        game = new Game();
        gameField.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        // connect model & view
        masterName.textProperty().bind(game.masterNameProperty());
        foodCount.textProperty().bind(game.foodCountProperty());
        Pair<Integer, Integer> gridSize = game.getGameFieldSize();
        int rows = gridSize.getKey();
        int columns = gridSize.getValue();
        final int CELL_SIZE = 400 / rows;
        fieldSize.setText(rows + "x" + columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                gameField.add(cell, i, j);
                GridPane.setMargin(cell, new Insets(0));
                ObjectProperty<CellState> state = new SimpleObjectProperty<>(CellState.EMPTY);
                state.addListener((observable, oldState, newState) -> {
                    switch (newState) {
                        case SNAKE -> cell.setFill(Color.WHITE);
                        case FOOD -> cell.setFill(Color.RED);
                        case EMPTY -> cell.setFill(Color.BLACK);
                    }
                });
                state.bind(game.CellStateProperty(i, j));
            }
        }
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> {
            game.update();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void handleKeyPressed(KeyEvent keyEvent) {
        System.out.println("keypressed");
        switch (keyEvent.getCode()) {
            case W -> game.setMasterSnakeDirection(SnakeDirection.UP);
            case S -> game.setMasterSnakeDirection(SnakeDirection.DOWN);
            case A -> game.setMasterSnakeDirection(SnakeDirection.LEFT);
            case D -> game.setMasterSnakeDirection(SnakeDirection.RIGHT);
        }
    }

    public void handleExitGameButton(ActionEvent actionEvent) throws IOException {
        // go to main screen
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        Stage currentStage = (Stage) gameField.getScene().getWindow();
        currentStage.setScene(new Scene(mainMenu));
    }
}
