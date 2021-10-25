package ru.nsu.ccfit.kokunina.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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
    }

    public void handleExitGameButton(ActionEvent actionEvent) throws IOException {
        // go to main screen
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        Stage currentStage = (Stage) gameField.getScene().getWindow();
        currentStage.setScene(new Scene(mainMenu));
    }
}
