package ru.nsu.ccfit.kokunina.net;

import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.game.SnakeDirection;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class NormalNetworkService extends Thread {
    private static final Logger log = LoggerFactory.getLogger(NormalNetworkService.class);

    private final DatagramSocket socket;
    private final SocketAddress masterAddress;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int BUFFER_SIZE = 1024;

    private final SimpleObjectProperty<SnakesProto.GameState> gameState;

    public NormalNetworkService(SocketAddress masterAddress) throws SocketException {
        this.masterAddress = masterAddress;
        socket = new DatagramSocket();
        socket.setSoTimeout(SOCKET_TIMEOUT);
        gameState = new SimpleObjectProperty<>();
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

    @Override
    public void run() {
        // listen new messages
        while (!isInterrupted()) {
            listen();
        }

    }

    private void listen() {
        log.debug("listen message on: " + socket.getLocalSocketAddress());
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket msgUdp = new DatagramPacket(buf, buf.length);
            socket.receive(msgUdp);
            byte[] msgBytes = Arrays.copyOf(msgUdp.getData(), msgUdp.getLength());
            log.info("receive: " + SnakesProto.GameMessage.parseFrom(msgBytes));
            SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(msgBytes);
            SocketAddress senderAddress = msgUdp.getSocketAddress();
            if (senderAddress.equals(masterAddress)) {
                log.info("receive from master node " + message.getTypeCase() + " message");
                processMsg(message);
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }

    private void processMsg(SnakesProto.GameMessage message) {
        switch (message.getTypeCase()) {
            case STATE -> processStateMsg(message.getState());
            case TYPE_NOT_SET -> log.error("Get message without a type.");
        }
    }

    private void processStateMsg(SnakesProto.GameMessage.StateMsg state) {
        gameState.set(state.getState());
        log.info(state.getState().toString());
    }

    public void setPlayerSnakeDirection(SnakeDirection direction) {
        // send steerMsg
    }

    public SimpleObjectProperty<SnakesProto.GameState> gameStateProperty() {
        return gameState;
    }
}
