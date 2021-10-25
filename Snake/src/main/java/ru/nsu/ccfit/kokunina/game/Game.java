package ru.nsu.ccfit.kokunina.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.util.Pair;
import ru.nsu.ccfit.kokunina.multicast.MulticastSender;

import java.net.UnknownHostException;

public class Game {
    private final StringProperty foodCount;
    private final StringProperty masterName;

    public Game() {
        foodCount = new SimpleStringProperty("300");
        masterName = new SimpleStringProperty("Danil");
        try {
            Thread sender = new Thread(new MulticastSender());
            sender.start();
        } catch (UnknownHostException e) {
            System.out.println("Can not start game");
            e.printStackTrace();
        }
    }

    public Pair<Integer, Integer> getGameFieldSize() {
        return new Pair<>(45, 70);
    }

    public StringProperty masterNameProperty() {
        return masterName;
    }

    public StringProperty foodCountProperty() {
        return foodCount;
    }
}
