package ru.nsu.ccfit.kokunina.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.game.SnakeDirection;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class MasterNetworkService extends Thread implements NetworkService {
    private static final Logger log = LoggerFactory.getLogger(MasterNetworkService.class);

    private static final int BUFFER_SIZE = 64 * 1024; // max size of UDP packet
    private static final int SOCKET_TIMEOUT = 5000;

    // listening socket for messages from other players
    private DatagramSocket socket;
    private final int PORT = 8888;
    private final Map<SocketAddress, NetworkNode> nodes;

    public MasterNetworkService() throws SocketException {
        socket = new DatagramSocket(PORT);
        socket.setSoTimeout(SOCKET_TIMEOUT);
        nodes = new HashMap<>();
    }

    @Override
    public void run() {
        while (!interrupted()) {
            listenMessages();
        }
        socket.close();
    }

    private void listenMessages() {
        try {
            DatagramPacket msgUdp = readMsgFromSocket();
            byte[] msgBytes = Arrays.copyOf(msgUdp.getData(), msgUdp.getLength());
            log.info("receive: " + SnakesProto.GameMessage.parseFrom(msgBytes));
            SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(msgBytes);
            SocketAddress senderAddress = msgUdp.getSocketAddress();
            if (!nodes.containsKey(senderAddress)) {
                log.info("new node: " + senderAddress);
                nodes.put(senderAddress, new NetworkNode());
            }
            processMsg(message, nodes.get(senderAddress));
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }

    private DatagramPacket readMsgFromSocket() throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket msgUdp = new DatagramPacket(buf, buf.length);
        socket.receive(msgUdp);
        return msgUdp;
    }

    private void processMsg(SnakesProto.GameMessage message, NetworkNode sender) {
        switch (message.getTypeCase()) {
            case PING -> processPingMsg(message.getPing(), sender);
            case STEER -> processSteerMsg(message.getSteer(), sender);
            case ACK -> processAckMsg(message.getAck(), sender);
            case STATE -> processStateMsg(message.getState(), sender);
            case ANNOUNCEMENT -> processAnnouncementMsg(message.getAnnouncement(), sender);
            case JOIN -> processJoinMsg(message.getJoin(), sender);
            case ERROR -> processErrorMsg(message.getError(), sender);
            case ROLE_CHANGE -> processRoleChangeMsg(message.getRoleChange(), sender);
            case TYPE_NOT_SET -> log.error("Get message without a type.");
        }
    }

    private void processPingMsg(SnakesProto.GameMessage.PingMsg pingMsg, NetworkNode sender) {
        sender.setLastOnline(System.currentTimeMillis());
    }

    private void processSteerMsg(SnakesProto.GameMessage.SteerMsg steer, NetworkNode sender) {
        SnakeDirection direction;
        switch (steer.getDirection()) {
            case UP -> direction = SnakeDirection.UP;
            case DOWN -> direction = SnakeDirection.DOWN;
            case LEFT -> direction = SnakeDirection.LEFT;
            case RIGHT -> direction = SnakeDirection.RIGHT;
            default -> throw new IllegalStateException("Unexpected value: " + steer.getDirection());
        }
        sender.setSnakeDirection(direction);
    }

    private void processAckMsg(SnakesProto.GameMessage.AckMsg ack, NetworkNode sender) {
        // error? master should send ackMsg
    }

    private void processStateMsg(SnakesProto.GameMessage.StateMsg state, NetworkNode sender) {
        // error? master should send stateMsg
    }

    private void processAnnouncementMsg(SnakesProto.GameMessage.AnnouncementMsg announcement, NetworkNode sender) {
        // error? master should send announcementMsg
    }

    private void processJoinMsg(SnakesProto.GameMessage.JoinMsg join, NetworkNode sender) {
        // add to players
        // send game state
    }

    private void processErrorMsg(SnakesProto.GameMessage.ErrorMsg error, NetworkNode sender) {
        // error? master should send errorMsg
    }

    private void processRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChange, NetworkNode sender) {
        //  1. от заместителя другим игрокам о том, что пора начинать считать его главным (sender_role = MASTER)
    }

}
