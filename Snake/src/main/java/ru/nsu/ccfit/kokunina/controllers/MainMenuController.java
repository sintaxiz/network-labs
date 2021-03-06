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
import ru.nsu.ccfit.kokunina.net.NetworkService;
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

            String playerName = EnterNamePopupController.askName();
            if (playerName == null) {
                return;
            }
            // TODO: add opportunity to enter config
            SnakesProto.GameConfig defaultConfig = SnakesProto.GameConfig.newBuilder().
                    setStateDelayMs(500).build();
            masterGameController.startGame(new GameConfig(playerName, defaultConfig),
                                    new NetworkService(masterGameController, defaultConfig, playerName, null,
                                            SnakesProto.NodeRole.MASTER));
            Stage newGameStage = (Stage) newGameButton.getScene().getWindow();
            newGameStage.setScene(new Scene(gameList));

        } catch (IOException e) {
            showAlertError("sorry, can not create game... ");
            log.error("can not create game", e);
        }

    }
    public void connectExistingGame(ActionEvent actionEvent) {
        log.debug("connectGame button pressed");
        try {
            Parent gameList = FXMLLoader.load(getClass().getClassLoader().getResource("game_list.fxml"));
            Stage newGameStage = (Stage) connectGameButton.getScene().getWindow();
            newGameStage.setScene(new Scene(gameList));
        } catch (IOException e) {
            showAlertError("sorry, can not show game list!!");
            log.error("can not create game list", e);
        }
    }

    private void showAlertError(String alertText) {
        Alert canNotStartGameAlert = new Alert(Alert.AlertType.ERROR);
        canNotStartGameAlert.setTitle("something wrong :c");
        canNotStartGameAlert.setContentText(alertText);
        canNotStartGameAlert.setAlertType(Alert.AlertType.ERROR);
        canNotStartGameAlert.show();
    }
}
