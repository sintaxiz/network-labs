package ru.nsu.ccfit.kokunina.net.multicast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

import static java.lang.Thread.interrupted;

public class MulticastSender implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MulticastSender.class);

    private final MulticastSocket out;
    private final byte[] msg;
    private final SocketAddress address;

    public MulticastSender(MulticastSocket out, byte[] msg, SocketAddress address) throws UnknownHostException {
        this.out = out;
        this.msg = msg;
        this.address = address;
    }

    @Override
    public void run() {
        try {
            while (!interrupted()) {
                out.send(new DatagramPacket(msg, msg.length, address));
                log.debug("send announcement to " + address);
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
