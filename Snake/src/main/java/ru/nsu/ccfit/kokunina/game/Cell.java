package ru.nsu.ccfit.kokunina.game;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

public class Cell {
    private ObjectProperty<CellState> state;

    public Cell() {
        state = new SimpleObjectProperty<>(CellState.EMPTY);
    }

    public ObservableValue<CellState> getCellStateProperty() {
        return state;
    }

    public void setState(CellState newState) {
        state.setValue(newState);
    }

    public CellState getState() {
        return state.get();
    }
}
