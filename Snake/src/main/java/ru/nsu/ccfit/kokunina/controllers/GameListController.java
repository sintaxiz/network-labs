package ru.nsu.ccfit.kokunina.controllers;

import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.net.NormalNetworkService;
import ru.nsu.ccfit.kokunina.net.multicast.AnnouncementsReceiver;

import java.io.IOException;
import java.net.*;
import java.util.ResourceBundle;

public class GameListController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(GameListController.class);

    @FXML
    public Button backButton;
    public Button joinGameButton;
    @FXML
    private ListView<GameListItem> listView;

    private AnnouncementsReceiver gameList;
    private GameListItem selectedGame = null;

    private int UPDATE_LIST_PERIOD = 2; // in seconds 
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            gameList = new AnnouncementsReceiver();
            gameList.start();
        } catch (IOException e) {
            System.out.println("can not create game list");
            e.printStackTrace();
            return;
        }
        listView.itemsProperty().bind(gameList.gamesProperty());
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<GameListItem> call(ListView<GameListItem> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(GameListItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getGameAddress().toString());
                        } else {
                            setText("");
                        }
                    }
                };
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            GameListItem gameItem = listView.getSelectionModel().getSelectedItem();
            if (gameItem != null) {
                System.out.println("selected: " + gameItem.getGameAddress().toString());
                joinGameButton.setVisible(true);
                selectedGame = gameItem;
            }
        });
    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        gameList.interrupt();
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.setScene(new Scene(mainMenu));
    }

    public void handleJoinGameButton(ActionEvent actionEvent) {
        try {
            if (selectedGame != null) {
                NormalNetworkService normalNetworkService = new NormalNetworkService();
                normalNetworkService.sendJoin(selectedGame.getGameAddress(), "Dasha");
                gameList.interrupt();
                FXMLLoader fxmlLoader = new FXMLLoader();
                Parent gameList = fxmlLoader.load(getClass().getClassLoader().getResource("game.fxml").openStream());
                //GameController gameController = fxmlLoader.getController();
                //gameController.startGame();
                Stage newGameStage = (Stage) joinGameButton.getScene().getWindow();
                newGameStage.setScene(new Scene(gameList));
                System.out.println("start game: " + selectedGame);
            }
        } catch (IOException e) {
            log.error("can not join game: " + e.getMessage());
            Alert canNotJoinGameAlert = new Alert(Alert.AlertType.ERROR);
            canNotJoinGameAlert.setTitle("something wrong :c");
            canNotJoinGameAlert.setContentText("sorry, can not join this game " + selectedGame.getGameAddress());
            canNotJoinGameAlert.show();
        }

    }
}
