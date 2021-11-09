package ru.nsu.ccfit.kokunina.controllers;

import ru.nsu.ccfit.kokunina.game.SnakeDirection;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

public interface GameController {
    void setGameState(SnakesProto.GameState state);

    void addPlayer(int uid);

    void setSnakeDirection(int snakeId, SnakeDirection direction);

    void normalPlayerToViewer(int senderId);

    void deputyToMaster();
}
