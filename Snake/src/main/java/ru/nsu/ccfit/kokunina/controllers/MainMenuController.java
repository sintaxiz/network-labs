package ru.nsu.ccfit.kokunina.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.game.GameConfig;
import ru.nsu.ccfit.kokunina.net.MasterNetworkService;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;

public class MainMenuController {
    private static Logger log = LoggerFactory.getLogger(MainMenuController.class);

    public Button newGameButton;
    public Button connectGameButton;

    public void startNewGame() {
        log.debug("startNewGame button pressed");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent gameList = fxmlLoader.load(getClass().getClassLoader().getResource("master_game.fxml").openStream());
            MasterGameController masterGameController = fxmlLoader.getController();
            // TODO: add opportunity to enter name and config
            SnakesProto.GameConfig defaultConfig = SnakesProto.GameConfig.newBuilder().build();
            masterGameController.startGame(new GameConfig("Sasha" , defaultConfig),
                                    new MasterNetworkService(defaultConfig));
            Stage newGameStage = (Stage) newGameButton.getScene().getWindow();
            newGameStage.setScene(new Scene(gameList));

        } catch (IOException e) {
            e.printStackTrace();
            Alert canNotStartGameAlert = new Alert(Alert.AlertType.ERROR);
            canNotStartGameAlert.setTitle("something wrong :c");
            canNotStartGameAlert.setContentText("sorry, can not create game... ");
            canNotStartGameAlert.show();
        }

    }
    public void connectExistingGame(ActionEvent actionEvent) {
        log.debug("connectGame button pressed");
        Parent gameList = null;
        try {
            gameList = FXMLLoader.load(getClass().getClassLoader().getResource("game_list.fxml"));
            Stage newGameStage = (Stage) connectGameButton.getScene().getWindow();
            newGameStage.setScene(new Scene(gameList));
        } catch (IOException e) {
            Alert canNotStartGameAlert = new Alert(Alert.AlertType.ERROR);
            canNotStartGameAlert.setTitle("something wrong :c");
            canNotStartGameAlert.setContentText("sorry, can not join game... " + e);
            canNotStartGameAlert.show();
        }
    }
}
