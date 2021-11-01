package ru.nsu.ccfit.kokunina.net.multicast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;

public class MulticastSender implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MulticastSender.class);

    final int PORT = 8888;
    InetAddress multicastAddress;
    byte[] message;

    public MulticastSender() throws UnknownHostException {
        this.multicastAddress = InetAddress.getByName("230.0.0.0");
        SnakesProto.GamePlayer me = SnakesProto.GamePlayer.newBuilder()
                .setName("me")
                .setId(123)
                .setIpAddress("1234")
                .setPort(123)
                .setRole(SnakesProto.NodeRole.MASTER)
                .setScore(0)
                .build();
        SnakesProto.GamePlayers players = SnakesProto.GamePlayers.newBuilder().addPlayers(me).build();
        SnakesProto.GameMessage.AnnouncementMsg announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                .setPlayers(players)
                .setConfig(SnakesProto.GameConfig.newBuilder().build())
                .build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(announcementMsg)
                .setMsgSeq(1).build();
        message = gameMessage.toByteArray();
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new MulticastSocket(PORT);
            while (!Thread.interrupted()) {
                socket.send(new DatagramPacket(message, message.length, multicastAddress, PORT));
                log.debug("sent: " + SnakesProto.GameMessage.parseFrom(message));
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
