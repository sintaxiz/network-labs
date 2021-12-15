package socks;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class SocksServer {
    private final int port;
    public static String SERVER_ADDRESS = "127.0.0.1";
    ArrayList<SocketChannel> connectedClients;

    public SocksServer(int port) {
        this.port = port;
    }

    public void serveConnections() throws IOException {
        System.out.println("Creating server socket");

        connectedClients = new ArrayList<>();

        final ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.bind(new InetSocketAddress(SERVER_ADDRESS, port));
        final Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Start serving");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                selector.select(1000);
                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();
                    keysIterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        accept(key);
                    }
                    if (key.isReadable()) {
                        read(key);
                    }
                    if (key.isWritable()) {
                        write(key);
                    }
                }
            } catch (IOException e) {
                System.out.println("Catch exception while serving: " + e.getMessage());
            }
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            socketChannel.write(ByteBuffer.wrap("yeeeeee im working! C:".getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            socketChannel.close();
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int read = socketChannel.read(buffer);
            System.out.println("Read data from " + socketChannel.getRemoteAddress() + ": "
                    + new String(buffer.array(), 0, read));
            byte[] msg = Arrays.copyOfRange(buffer.array(), 0, read);
            if (connectedClients.contains(socketChannel)) {
                // expecting command in msg
                // Client sends a connection request
                // VER (1)	CMD (1)	RSV (1)	DSTADDR (var) DSTPORT (2)
                byte ver = msg[0];
                byte cmd = msg[1];
                // 0x01: establish a TCP/IP stream connection
                if (cmd == 0x01) {
                    // TODO: parse dstaddr

                    // TODO: establish connection with dstaddr


                    // Response packet from server 	VER STATUS RSV BNDADDR BNDPORT

                } else {
                    // not supported operation
                }
                // TODO: rsv == 0x00 reserved byte

                int dstPort = msg[3] | msg[4] << 8;     //port number in a network byte order
            } else {
                // expecting establish connection msg

                // Client connects and sends a greeting, which includes a list of authentication methods supported.
                byte socksVersion = msg[0];

                // ignoring NAUTH & AUTH because No authentication
                // byte numberAuth = msg[1];
                // byte auth = msg[2...];

                // Server chooses one of the methods (or sends a failure response if none of them are acceptable).
                // TODO: there need to send 0x05 0x00
                socketChannel.register(key.selector(), SelectionKey.OP_WRITE);

                connectedClients.add(socketChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            socketChannel.close();
        }
    }

    // Добавляет нового клиента
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_CONNECT);
        System.out.println("Add new connection: " + socketChannel.getRemoteAddress());
    }
}
