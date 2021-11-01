package ru.nsu.ccfit.kokunina.net.multicast;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.nsu.ccfit.kokunina.controllers.GameListItem;
import ru.nsu.ccfit.kokunina.snakes.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class GameList {
    private final SimpleListProperty<GameListItem> games;

    private MulticastSocket socket;
    private int BUFFER_SIZE = 256;
    private int SOCKET_TIMEOUT = 4000;
    public GameList() throws IOException {
        games = new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>()));
        socket = new MulticastSocket(4000);
        InetAddress multicastAddress = InetAddress.getByName("230.0.0.0");
        SocketAddress socketAddress = new InetSocketAddress(multicastAddress, 0);
        socket.joinGroup(socketAddress, NetworkInterface.getByName("lo"));
        //socket.leaveGroup(socketAddress, NetworkInterface.getByName("lo"));
    }

    public void findAvailableGames() {
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket msg = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            System.out.println("going to receive datagram");
            socket.receive(msg);
            byte[] buf2 = Arrays.copyOf(msg.getData(), msg.getLength());
            System.out.println(Arrays.toString(buf2));
            System.out.println("receive: " + SnakesProto.GameMessage.parseFrom(buf2));
            SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(buf2);

            String gameIp = msg.getAddress().toString();
            if (!games.get().contains(gameIp)) {
                games.get().add(new GameListItem(gameIp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableValue<ObservableList<GameListItem>> gamesProperty() {
        return games;
    }
}
