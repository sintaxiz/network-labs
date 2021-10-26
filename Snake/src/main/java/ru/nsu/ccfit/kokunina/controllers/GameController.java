package ru.nsu.ccfit.kokunina.controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import ru.nsu.ccfit.kokunina.game.CellState;
import ru.nsu.ccfit.kokunina.game.Game;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    public ListView<Text> playerList;
    public Text masterName;
    public Text fieldSize;
    public Text foodCount;
    public GridPane gameField;

    private Game game;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        game = new Game();
        // connect model & view
        masterName.textProperty().bind(game.masterNameProperty());
        foodCount.textProperty().bind(game.foodCountProperty());
        Pair<Integer, Integer> gridSize = game.getGameFieldSize();
        int rows = gridSize.getKey();
        int columns = gridSize.getValue();
        final double CELL_SIZE = 800 / rows;


        fieldSize.setText(rows + "x" + columns);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                gameField.add(cell, i, j);
                ObjectProperty<CellState> state = new SimpleObjectProperty<>(CellState.EMPTY);
                state.bind(game.getCellStateProperty(i, j));
                state.addListener((observableCell, oldState, newState) -> {
                    switch (newState) {
                        case SNAKE -> cell.setFill(Color.WHITE);
                        case FOOD -> cell.setFill(Color.RED);
                        case EMPTY -> cell.setFill(Color.BLACK);
                    }
                });
            }
        }
    }

    public void handleExitGameButton(ActionEvent actionEvent) throws IOException {
        // go to main screen
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        Stage currentStage = (Stage) gameField.getScene().getWindow();
        currentStage.setScene(new Scene(mainMenu));
    }
}
