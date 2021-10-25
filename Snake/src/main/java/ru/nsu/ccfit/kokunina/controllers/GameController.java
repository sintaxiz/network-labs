package ru.nsu.ccfit.kokunina.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import ru.nsu.ccfit.kokunina.game.Game;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    public ListView<Text> playerList;
    public Text masterName;
    public Text fieldSize;
    public Text foodCount;
    public TilePane gameField;

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
        fieldSize.setText(rows + "x" + columns);
        gameField.setMinSize(columns * 11, rows * 10);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                gameField.getChildren().add(new Rectangle(10,10));
            }
        }
        // bind
        // add listener


    }

    public void handleExitGameButton(ActionEvent actionEvent) throws IOException {
        // go to main screen
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        Stage currentStage = (Stage) gameField.getScene().getWindow();
        currentStage.setScene(new Scene(mainMenu));
    }
}
