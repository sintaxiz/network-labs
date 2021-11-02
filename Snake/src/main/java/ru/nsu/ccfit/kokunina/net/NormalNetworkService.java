package ru.nsu.ccfit.kokunina.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;

public class NormalNetworkService {
    private static final Logger log = LoggerFactory.getLogger(NormalNetworkService.class);

    private final DatagramSocket socket;
    private static final int SOCKET_TIMEOUT = 5000;

    public NormalNetworkService() throws SocketException {
        socket = new DatagramSocket();
        socket.setSoTimeout(SOCKET_TIMEOUT);
    }

    private SnakesProto.GameMessage.JoinMsg createJoinMsg(String playerName) {
        return SnakesProto.GameMessage.JoinMsg.newBuilder().setName(playerName).build();
    }

    public void sendJoin(SocketAddress receiver, String playerName) throws IOException {
        byte[] buf = SnakesProto.GameMessage.newBuilder()
                .setJoin(createJoinMsg(playerName))
                .setMsgSeq(1)
                .build().toByteArray();
        DatagramPacket msg = new DatagramPacket(buf, buf.length, receiver);
        socket.send(msg);
        log.info("send join msg to " + receiver);
    }
}
