package ru.nsu.ccfit.kokunina.net;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MasterNetworkService extends Thread {
    // listening socket for messages from other players
    private DatagramSocket socket;
    private final int PORT = 8888;

    public MasterNetworkService() throws SocketException {
        socket = new DatagramSocket(PORT);
    }

    @Override
    public void run() {
        super.run();
    }
}
