package ru.nsu.ccfit.kokunina.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import ru.nsu.ccfit.kokunina.net.multicast.GameList;

import java.io.IOException;
import java.net.*;
import java.util.ResourceBundle;

public class GameListController implements Initializable {

    @FXML
    public Button backButton;
    public Button joinGameButton;
    @FXML
    private ListView<GameListItem> listView;

    private GameList gameList;
    private GameListItem selectedGame = null;

    private int UPDATE_LIST_PERIOD = 2; // in seconds 
    private Timeline timeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            gameList = new GameList();
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
                            setText(item.getIp());
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
                System.out.println("selected: " + gameItem.getIp());
                joinGameButton.setVisible(true);
                selectedGame = gameItem;
            }
        });
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(UPDATE_LIST_PERIOD),
                        actionEvent -> {
                            update();
                        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void update() {
        gameList.findAvailableGames();
    }

    public void handleBackButton(ActionEvent actionEvent) throws IOException {
        timeline.stop();
        Parent mainMenu = FXMLLoader.load(getClass().getClassLoader().getResource("main_menu.fxml"));
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.setScene(new Scene(mainMenu));
    }

    public void handleJoinGameButton(ActionEvent actionEvent) {
        if (selectedGame != null) {
            System.out.println("start game: " + selectedGame);
        }
    }
}
