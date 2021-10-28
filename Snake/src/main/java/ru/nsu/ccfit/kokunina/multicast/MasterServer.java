package ru.nsu.ccfit.kokunina.multicast;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MasterServer extends Thread {
    // listening socket for messages from other players
    private DatagramSocket socket;
    private final int PORT = 8888;

    public MasterServer() throws SocketException {
        socket = new DatagramSocket(PORT);
    }

    @Override
    public void run() {
        super.run();
    }
}
