package ru.nsu.ccfit.kokunina.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.controllers.GameController;
import ru.nsu.ccfit.kokunina.game.Coordinates;
import ru.nsu.ccfit.kokunina.game.Snake;
import ru.nsu.ccfit.kokunina.game.SnakeDirection;
import ru.nsu.ccfit.kokunina.net.multicast.MulticastSender;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class NetworkService extends Thread {

    private static final Logger log = LoggerFactory.getLogger(NetworkService.class);

    private static final int BUFFER_SIZE = 64 * 1024; // max size of UDP packet
    private static final int SOCKET_TIMEOUT = 5000;

    // listening socket for messages from other players
    private MulticastSocket socket;
    private final Map<SocketAddress, NetworkNode> nodes;
    private SnakesProto.GamePlayer thisPlayer;
    private SocketAddress masterAddress;

    private final GameController gameController;
    private final SnakesProto.GameConfig gameConfig;
    private List<SnakesProto.GameState.Snake> snakes;
    private List<SnakesProto.GameState.Coord> foods;
    private HashMap<Integer, SnakesProto.GamePlayer> players;
    private int uid = 0;

    private int stateOrder = 0;
    private int msgSeq = 0;
    private final HashMap<Long, SnakesProto.GameMessage> messagesToSent;

    Thread multicastSender;

    private final String MULTICAST_ADDRESS = "239.192.0.4";
    final int MULTICAST_PORT = 9192;
    InetAddress multicastAddress;
    byte[] message;

    public NetworkService(GameController gameController, SnakesProto.GameConfig gameConfig,
                          String userName, SocketAddress masterAddress, SnakesProto.NodeRole initRole)
            throws IOException {
        this.gameController = gameController;
        this.gameConfig = gameConfig;
        this.masterAddress = masterAddress;
        socket = new MulticastSocket();
        messagesToSent = new HashMap<Long, SnakesProto.GameMessage>();

        //SocketAddress socketAddress = new InetSocketAddress(multicastAddress, MULTICAST_PORT);
        //socket.joinGroup(socketAddress, NetworkInterface.getByName("lo"));
        socket.setSoTimeout(SOCKET_TIMEOUT);
        multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
        nodes = new HashMap<>();

        thisPlayer = SnakesProto.GamePlayer.newBuilder()
                .setName(userName)
                .setId(0)
                .setIpAddress("")
                .setPort(socket.getPort())
                .setRole(initRole)
                .setScore(0)
                .build();

        SnakesProto.GamePlayers players = SnakesProto.GamePlayers.newBuilder().addPlayers(thisPlayer).build();
        SnakesProto.GameMessage.AnnouncementMsg announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                .setPlayers(players)
                .setConfig(SnakesProto.GameConfig.newBuilder().build())
                .build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(announcementMsg)
                .setMsgSeq(1).build();
        message = gameMessage.toByteArray();

        snakes = new ArrayList<>();
        this.players = new HashMap<>();
        // need to add master snake & player

        this.players.put(0, thisPlayer);
    }

    @Override
    public void run() {
        log.debug(thisPlayer.getRole().toString());
        if (thisPlayer.getRole() == SnakesProto.NodeRole.MASTER) {
            try {
                multicastSender =
                        new Thread(new MulticastSender(socket, message, new InetSocketAddress(multicastAddress, MULTICAST_PORT)));
                multicastSender.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.debug("start listen messages");
        while (!interrupted()) {
            listenMessages();
        }
        if (thisPlayer.getRole() == SnakesProto.NodeRole.MASTER) {
            assert multicastSender != null;
            multicastSender.interrupt();
        }
        socket.close();

    }

    private SnakesProto.GameMessage.JoinMsg createJoinMsg(String playerName) {
        return SnakesProto.GameMessage.JoinMsg.newBuilder().setName(playerName).build();
    }

    public void sendJoin(SocketAddress receiver, String playerName) throws IOException {
        log.info("sending join msg to " + receiver + "...");
        byte[] buf = SnakesProto.GameMessage.newBuilder()
                .setJoin(createJoinMsg(playerName))
                .setMsgSeq(msgSeq++)
                .build().toByteArray();
        DatagramPacket msg = new DatagramPacket(buf, buf.length, receiver);
        socket.send(msg);
        log.info("send join msg to " + receiver);
    }

    public void sendGameState(SnakesProto.GameState gameState, SocketAddress receiver) throws IOException {
        log.info("sending game state to " + receiver + "...");
        SnakesProto.GameMessage.StateMsg stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder().setState(gameState).build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder().setState(stateMsg).setMsgSeq(msgSeq++).build();
        byte[] msgBytes = gameMessage.toByteArray();
        DatagramPacket udpMsg = new DatagramPacket(msgBytes, msgBytes.length, receiver);
        socket.send(udpMsg);
        messagesToSent.put(gameMessage.getMsgSeq(), gameMessage);
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
            case PING -> processPingMsg(message, sender);
            case STEER -> processSteerMsg(message, sender);
            case ACK -> processAckMsg(message, sender);
            case STATE -> processStateMsg(message, sender);
            case ANNOUNCEMENT -> processAnnouncementMsg(message, sender);
            case JOIN -> processJoinMsg(message, sender);
            case ERROR -> processErrorMsg(message, sender);
            case ROLE_CHANGE -> processRoleChangeMsg(message, sender);
            case TYPE_NOT_SET -> log.error("Get message without a type.");
        }
    }

    private void processPingMsg(SnakesProto.GameMessage pingMsg, NetworkNode sender) {
        sender.setLastOnline(System.currentTimeMillis());
    }

    private void processSteerMsg(SnakesProto.GameMessage steer, NetworkNode sender) {
        SnakeDirection direction;
        switch (steer.getSteer().getDirection()) {
            case UP -> direction = SnakeDirection.UP;
            case DOWN -> direction = SnakeDirection.DOWN;
            case LEFT -> direction = SnakeDirection.LEFT;
            case RIGHT -> direction = SnakeDirection.RIGHT;
            default -> throw new IllegalStateException("Unexpected value: " + steer.getSteer().getDirection());
        }
        sender.setSnakeDirection(direction);
        gameController.setSnakeDirection(sender.getId(), direction);
    }

    private void processAckMsg(SnakesProto.GameMessage ack, NetworkNode sender) {
        messagesToSent.entrySet().removeIf(e -> e.getValue().getMsgSeq() == ack.getMsgSeq() &&
                e.getValue().getSenderId() == ack.getSenderId());
    }

    private void processStateMsg(SnakesProto.GameMessage state, NetworkNode sender) throws IOException {
        if (sender.getAddress().equals(masterAddress)) {
            gameController.setGameState(state.getState().getState());
            log.info("change game state");
        }
        sendAck(sender.getAddress(), state.getMsgSeq());
    }

    private void processAnnouncementMsg(SnakesProto.GameMessage announcement, NetworkNode sender) {
        // ignore because game list controller listen for announcements
    }

    private void processJoinMsg(SnakesProto.GameMessage join, NetworkNode sender) throws IOException {
        // add to players
        // send game state
        gameController.addPlayer(uid++);
        addPlayer(uid, join.getJoin().getName(), 2, "3");
        sendGameState(createStateMsg(), sender.getAddress());
        sendAck(sender.getAddress(), join.getMsgSeq());
    }

    private void addPlayer(int uid, String name, int port, String ip) {
        SnakesProto.NodeRole role = players.size() == 1 ? SnakesProto.NodeRole.DEPUTY : SnakesProto.NodeRole.NORMAL;
        players.put(uid, SnakesProto.GamePlayer.newBuilder()
                .setId(uid)
                .setName(name)
                .setPort(port)
                .setIpAddress(ip)
                .setRole(role)
                .setScore(0)
                .build());
    }

    private void processErrorMsg(SnakesProto.GameMessage error, NetworkNode sender) throws IOException {
        String errorMessage = error.getError().getErrorMessage();
        log.error("ErrorMsg: " + errorMessage);
        sendAck(sender.getAddress(), error.getMsgSeq());
    }

    private void sendAck(SocketAddress reciever, long msgseq) throws IOException {
        SnakesProto.GameMessage.AckMsg ackMessage = SnakesProto.GameMessage.AckMsg.newBuilder()
                                                    .build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setAck(ackMessage)
                .setMsgSeq(msgseq)
                .build();
        byte[] msg = gameMessage.toByteArray();
        DatagramPacket datagramPacket = new DatagramPacket(msg, msg.length, reciever);
        socket.send(datagramPacket);
    }

    private void processRoleChangeMsg(SnakesProto.GameMessage roleChange, NetworkNode sender) {
        SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = roleChange.getRoleChange();
        //  1. от заместителя другим игрокам о том, что пора начинать считать его главным (sender_role = MASTER)
        if (roleChangeMsg.getSenderRole() == SnakesProto.NodeRole.MASTER) {
            masterAddress = sender.getAddress();
        }
        // 2. от осознанно выходящего игрока (sender_role = VIEWER)
        if (roleChangeMsg.getSenderRole() == SnakesProto.NodeRole.VIEWER) {
            gameController.normalPlayerToViewer(roleChange.getSenderId());
        }
        // 4. в комбинации с 1,2 или отдельно от них: назначение кого-то заместителем (receiver_role = DEPUTY)
        if (roleChangeMsg.getReceiverRole() == SnakesProto.NodeRole.DEPUTY) {
            thisPlayer = thisPlayer.toBuilder().setRole(SnakesProto.NodeRole.DEPUTY).build();
        }
        //  5. в комбинации с 2 от главного узла заместителю о том, что он становится главным (receiver_role = MASTER)
        if (roleChangeMsg.getReceiverRole() == SnakesProto.NodeRole.MASTER && thisPlayer.getRole() == SnakesProto.NodeRole.DEPUTY) {
            gameController.deputyToMaster();
        }
    }


    public void notifyNetwork(ArrayList<Coordinates> foodsCoord, ArrayList<Snake> gameSnakes) throws IOException {
        // create and send current game state
        foods = toCoords(foodsCoord);
        snakes = toSnakes(gameSnakes);
        SnakesProto.GameState gameState = createStateMsg();
        for (Map.Entry<SocketAddress, NetworkNode> node : nodes.entrySet()) {
            sendGameState(gameState, node.getKey());
        }
    }

    private SnakesProto.GameState createStateMsg() {
        return SnakesProto.GameState.newBuilder()
                .setStateOrder(stateOrder++)
                .setPlayers(SnakesProto.GamePlayers.newBuilder().addAllPlayers(players.values()))
                .addAllSnakes(snakes)
                .addAllFoods(foods)
                .setConfig(gameConfig)
                .build();
    }

    private List<SnakesProto.GameState.Coord> toCoords(List<Coordinates> foodsCoord) {
        ArrayList<SnakesProto.GameState.Coord> coords = new ArrayList<>();
        for (Coordinates coord : foodsCoord) {
            coords.add(SnakesProto.GameState.Coord.newBuilder()
                    .setX(coord.getX())
                    .setY(coord.getY())
                    .build());
        }
        return coords;
    }

    private ArrayList<SnakesProto.GameState.Snake> toSnakes(ArrayList<Snake> snakes) {
        ArrayList<SnakesProto.GameState.Snake> snakesProto = new ArrayList<>();
        for (Snake snake : snakes) {
            SnakesProto.GamePlayer player = players.get(snake.getId());
            if (player != null) {
                SnakesProto.GameState.Snake.SnakeState state = SnakesProto.GameState.Snake.SnakeState.ALIVE;
                int playerId = player.getId();
                players.replace(playerId, player.toBuilder().setScore(snake.getScore()).build());
            }
            SnakesProto.GameState.Snake.SnakeState state = player == null ? SnakesProto.GameState.Snake.SnakeState.ZOMBIE
                    : SnakesProto.GameState.Snake.SnakeState.ALIVE;
            SnakesProto.Direction direction;
            switch (snake.getDirection()) {
                case UP -> direction = SnakesProto.Direction.UP;
                case DOWN -> direction = SnakesProto.Direction.DOWN;
                case LEFT -> direction = SnakesProto.Direction.LEFT;
                case RIGHT -> direction = SnakesProto.Direction.RIGHT;
                default -> throw new IllegalStateException("Unexpected value: " + snake.getDirection());
            }
            snakesProto.add(SnakesProto.GameState.Snake.newBuilder()
                    .setState(state)
                    .setPlayerId(snake.getId())
                    .addAllPoints(toCoords(snake.getCoord()))
                    .setHeadDirection(direction)
                    .build());
        }
        return snakesProto;
    }

    public void setPlayerSnakeDirection(int snakeId, SnakeDirection direction) throws IOException {
        // send steerMsg
        SnakesProto.GamePlayer gamePlayer = players.get(snakeId);
        log.debug("ip address: " + gamePlayer.getIpAddress() + ":" + gamePlayer.getPort());

        SnakesProto.GameMessage.SteerMsg steerMsg = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(toProtoDirection(direction))
                .build();
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMsg)
                .setMsgSeq(msgSeq)
                .setSenderId(snakeId)
                .build();

        byte[] buf = gameMessage.toByteArray();
        DatagramPacket udpMsg = new DatagramPacket(buf, buf.length, new InetSocketAddress(gamePlayer.getIpAddress(),
                gamePlayer.getPort()));
        socket.send(udpMsg);
    }

    private SnakesProto.Direction toProtoDirection(SnakeDirection direction) {
        SnakesProto.Direction protoDir;
        switch (direction) {
            case UP -> protoDir = SnakesProto.Direction.UP;
            case DOWN -> protoDir = SnakesProto.Direction.DOWN;
            case LEFT -> protoDir = SnakesProto.Direction.LEFT;
            case RIGHT -> protoDir = SnakesProto.Direction.RIGHT;
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
        return protoDir;
    }

    public int getPlayerId() {
        return thisPlayer.getId();
    }
}
