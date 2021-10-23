package ru.nsu.ccfit.kokunina.controllers;

import javafx.fxml.Initializable;
import ru.nsu.ccfit.kokunina.game.Game;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    private Game game;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        game = new Game();
    }
}
