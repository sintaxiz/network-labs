package ru.nsu.ccfit.kokunina.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.game.Coordinates;
import ru.nsu.ccfit.kokunina.game.Snake;
import ru.nsu.ccfit.kokunina.game.SnakeDirection;
import ru.nsu.ccfit.kokunina.net.multicast.MulticastSender;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class MasterNetworkService extends Thread {

    private static final Logger log = LoggerFactory.getLogger(MasterNetworkService.class);

    private static final int BUFFER_SIZE = 64 * 1024; // max size of UDP packet
    private static final int SOCKET_TIMEOUT = 5000;

    // listening socket for messages from other players
    private MulticastSocket socket;
    private final Map<SocketAddress, NetworkNode> nodes;
    private SnakesProto.GamePlayer masterPlayer;

    private final SnakesProto.GameConfig gameConfig;
    private final ArrayList<SnakesProto.GameState.Snake> snakes;
    private ArrayList<SnakesProto.GameState.Coord> foods;
    private ArrayList<SnakesProto.GamePlayer> players;
    private int stateOrder = 0;

    private final String MULTICAST_ADDRESS = "239.192.0.4";
    final int MULTICAST_PORT = 9192;
    InetAddress multicastAddress;
    byte[] message;

    public MasterNetworkService(SnakesProto.GameConfig gameConfig) throws IOException {
        this.gameConfig = gameConfig;
        socket = new MulticastSocket();

        //SocketAddress socketAddress = new InetSocketAddress(multicastAddress, MULTICAST_PORT);
        //socket.joinGroup(socketAddress, NetworkInterface.getByName("lo"));
        socket.setSoTimeout(SOCKET_TIMEOUT);
        multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
        nodes = new HashMap<>();

        masterPlayer = SnakesProto.GamePlayer.newBuilder()
                .setName("player_name")
                .setId(0)
                .setIpAddress("")
                .setPort(socket.getPort())
                .setRole(SnakesProto.NodeRole.MASTER)
                .setScore(0)
                .build();

        SnakesProto.GamePlayers players = SnakesProto.GamePlayers.newBuilder().addPlayers(masterPlayer).build();
        SnakesProto.GameMessage.AnnouncementMsg announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                .setPlayers(players)
                .setConfig(SnakesProto.GameConfig.newBuilder().build())
                .build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(announcementMsg)
                .setMsgSeq(1).build();
        message = gameMessage.toByteArray();

        snakes = new ArrayList<>();
        this.players = new ArrayList<>();
        // need to add master snake & player

        this.players.add(masterPlayer);
        snakes.add(SnakesProto.GameState.Snake.newBuilder()
                .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE)
                .setPlayerId(100)
                .setHeadDirection(SnakesProto.Direction.UP)
                .addPoints(SnakesProto.GameState.Coord.newBuilder()
                        .setX(10).setY(2).build())
                .build());
    }

    @Override
    public void run() {
        Thread multicastSender = null;
        try {
            multicastSender =
                    new Thread(new MulticastSender(socket, message, new InetSocketAddress(multicastAddress, MULTICAST_PORT)));
            multicastSender.start();
            while (!interrupted()) {
                listenMessages();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert multicastSender != null;
            multicastSender.interrupt();
        }
    }

    public void sendGameState(SnakesProto.GameState gameState, SocketAddress receiver) throws IOException {
        SnakesProto.GameMessage.StateMsg stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder().setState(gameState).build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder().setState(stateMsg).setMsgSeq(2).build();
        byte[] msgBytes = gameMessage.toByteArray();
        DatagramPacket udpMsg = new DatagramPacket(msgBytes, msgBytes.length, receiver);
        socket.send(udpMsg);
        log.info("send game state to " + receiver);
    }

    private void listenMessages() {
        log.debug("listen message on: " + socket.getLocalSocketAddress());
        try {
            DatagramPacket msgUdp = readMsgFromSocket();
            byte[] msgBytes = Arrays.copyOf(msgUdp.getData(), msgUdp.getLength());
            log.info("receive: " + SnakesProto.GameMessage.parseFrom(msgBytes));
            SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(msgBytes);
            SocketAddress senderAddress = msgUdp.getSocketAddress();
            if (!nodes.containsKey(senderAddress)) {
                log.info("new node: " + senderAddress);
                nodes.put(senderAddress, new NetworkNode(senderAddress));
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

    private void processMsg(SnakesProto.GameMessage message, NetworkNode sender) throws IOException {
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

    private void processJoinMsg(SnakesProto.GameMessage.JoinMsg join, NetworkNode sender) throws IOException {
        // add to players
        // send game state
        sendGameState(createStateMsg(), sender.getAddress());
    }

    private void processErrorMsg(SnakesProto.GameMessage.ErrorMsg error, NetworkNode sender) {
        // error? master should send errorMsg
    }

    private void processRoleChangeMsg(SnakesProto.GameMessage.RoleChangeMsg roleChange, NetworkNode sender) {
        //  1. от заместителя другим игрокам о том, что пора начинать считать его главным (sender_role = MASTER)
    }

    private void findFreeSpaceForSnake() {

    }

    private int tmp = 0;
    public void notifyNetwork(ArrayList<Coordinates> foodsCoord, ArrayList<Snake> snakes) throws IOException {
        // create and send current game state
        foods = toCoords(foodsCoord);
        SnakesProto.GameState gameState = createStateMsg();
        for (Map.Entry<SocketAddress, NetworkNode> node : nodes.entrySet()) {
            sendGameState(gameState, node.getKey());
        }
        tmp = (tmp + 1) % 10;
    }

    private SnakesProto.GameState createStateMsg() {
        return SnakesProto.GameState.newBuilder()
                .setStateOrder(stateOrder++)
                .setPlayers(SnakesProto.GamePlayers.newBuilder().addAllPlayers(players))
                .addAllSnakes(snakes)
                .addAllFoods(foods)
                .setConfig(gameConfig)
                .build();
    }

    private ArrayList<SnakesProto.GameState.Coord> toCoords(ArrayList<Coordinates> foodsCoord) {
        ArrayList<SnakesProto.GameState.Coord> coords = new ArrayList<>();
        for (Coordinates coord : foodsCoord) {
            coords.add(SnakesProto.GameState.Coord.newBuilder()
                    .setX(coord.getX())
                    .setY(coord.getY())
                    .build());
        }
        return coords;
    }
}
