package ru.nsu.ccfit.kokunina.game;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Pair;
import ru.nsu.ccfit.kokunina.multicast.MulticastSender;

import java.net.UnknownHostException;

public class Game {
    private final StringProperty foodCount;
    private final StringProperty masterName;
    private final Cell[][] cells;

    public Game() {
        foodCount = new SimpleStringProperty("300");
        masterName = new SimpleStringProperty("Danil");
        cells = new Cell[40][30];
        for (int i = 0; i < 40; i++) {
            for (int j = 0; j < 30; j++) {
                cells[i][j] = new Cell();
            }
        }
        try {
            Thread sender = new Thread(new MulticastSender());
            sender.start();
        } catch (UnknownHostException e) {
            System.out.println("Can not start game");
            e.printStackTrace();
        }
    }

    public Pair<Integer, Integer> getGameFieldSize() {
        return new Pair<>(cells.length, cells[0].length);
    }

    public StringProperty masterNameProperty() {
        return masterName;
    }

    public StringProperty foodCountProperty() {
        return foodCount;
    }

    public ObservableValue<CellState> getCellStateProperty(int x, int y) {
        return cells[x][y].getCellStateProperty();
    }
}
