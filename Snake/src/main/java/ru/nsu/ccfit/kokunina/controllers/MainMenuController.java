package ru.nsu.ccfit.kokunina.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainMenuController {
    private static Logger log = LoggerFactory.getLogger(MainMenuController.class);

    public Button newGameButton;
    public Button connectGameButton;

    public void startNewGame() throws IOException {
        log.debug("startNewGame button pressed");
        Parent gameList = FXMLLoader.load(getClass().getClassLoader().getResource("game.fxml"));
        Stage newGameStage = (Stage) newGameButton.getScene().getWindow();
        newGameStage.setScene(new Scene(gameList));
    }
    public void connectExistingGame(ActionEvent actionEvent) throws IOException {
        log.debug("connectGame button pressed");
        Parent gameList = FXMLLoader.load(getClass().getClassLoader().getResource("game_list.fxml"));
        Stage newGameStage = (Stage) connectGameButton.getScene().getWindow();
        newGameStage.setScene(new Scene(gameList));
    }
}
