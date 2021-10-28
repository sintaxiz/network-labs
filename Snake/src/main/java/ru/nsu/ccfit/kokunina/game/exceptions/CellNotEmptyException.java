package ru.nsu.ccfit.kokunina.game.exceptions;

public class CellNotEmptyException extends Exception {
    public CellNotEmptyException() {
    }

    public CellNotEmptyException(String message) {
        super(message);
    }

    public CellNotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public CellNotEmptyException(Throwable cause) {
        super(cause);
    }

    public CellNotEmptyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
