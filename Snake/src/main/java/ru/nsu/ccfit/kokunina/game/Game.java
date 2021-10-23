package ru.nsu.ccfit.kokunina.game;

import ru.nsu.ccfit.kokunina.multicast.MulticastSender;

import java.net.UnknownHostException;

public class Game {
    public Game() {

        try {
            Thread sender = new Thread(new MulticastSender());
            sender.start();
        } catch (UnknownHostException e) {
            System.out.println("Can not start game");
            e.printStackTrace();
        }
    }
}
