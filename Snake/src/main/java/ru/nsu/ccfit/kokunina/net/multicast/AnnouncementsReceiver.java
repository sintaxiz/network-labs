package ru.nsu.ccfit.kokunina.net.multicast;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.ccfit.kokunina.controllers.GameListItem;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class AnnouncementsReceiver extends Thread {
    private final Logger log = LoggerFactory.getLogger(AnnouncementsReceiver.class);

    private final SimpleListProperty<GameListItem> games;

    private MulticastSocket socket;
    private SocketAddress socketAddress;
    private final int BUFFER_SIZE = 256;
    private final int SOCKET_TIMEOUT = 5000;
    private final int MULTICAST_PORT = 9192;
    private final String MULTICAST_ADDRESS =  "239.192.0.4";

    private final ArrayList<String> addresses;

    public AnnouncementsReceiver() throws IOException {
        games = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
        socket = new MulticastSocket(MULTICAST_PORT);
        socket.setSoTimeout(SOCKET_TIMEOUT);
        InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
        socketAddress = new InetSocketAddress(multicastAddress, MULTICAST_PORT);
        socket.joinGroup(socketAddress, NetworkInterface.getByName("lo"));
        //socket.leaveGroup(socketAddress, NetworkInterface.getByName("lo"));
        addresses = new ArrayList<>();
    }

    public void findAvailableGames() {
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket msg = new DatagramPacket(buf, buf.length);
            System.out.println("going to receive datagram");
            socket.receive(msg);
            byte[] buf2 = Arrays.copyOf(msg.getData(), msg.getLength());
            System.out.println(Arrays.toString(buf2));
            System.out.println("receive: " + SnakesProto.GameMessage.parseFrom(buf2));
            SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(buf2);
            if (gameMessage.getTypeCase() == SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT) {
                String gameIp = msg.getSocketAddress().toString();
                if (!addresses.contains(gameIp)) {
                    addresses.add(gameIp);
                    games.get().add(new GameListItem(msg.getSocketAddress()));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ObservableValue<ObservableList<GameListItem>> gamesProperty() {
        return games;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            findAvailableGames();
        }
    }
}
