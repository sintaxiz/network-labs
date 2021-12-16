package socks;

import socks.exceptions.WrongSocksMessageException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class SocksServer {
    private final int port;
    public static String SERVER_ADDRESS = "127.0.0.1";

    public SocksServer(int port) {
        this.port = port;
    }

    public void serveConnections() throws IOException {
        System.out.println("Creating server socket");

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
            TcpConnection tcpConnection = (TcpConnection) key.attachment();
            socketChannel.write(ByteBuffer.wrap("yeeeeee im working! C:".getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            socketChannel.close();
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            TcpConnection tcpConnection = (TcpConnection) key.attachment();
            switch (tcpConnection.currentState) {
                case WAITING_FOR_GREETINGS -> tcpConnection.readGreeting();
                case WAITING_FOR_COMMAND -> tcpConnection.readCommandRequest();
            }
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            readBuffer.clear();
            int read = socketChannel.read(readBuffer);
            readBuffer.flip();
            byte[] data = new byte[1000];
            readBuffer.get(data, 0, read);
            System.out.println("Received: " + new String(data, 0, read));
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException | WrongSocksMessageException e) {
            e.printStackTrace();
            key.cancel();
        }
    }

    // Добавляет нового клиента
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register( key.selector(), SelectionKey.OP_READ,
                                new TcpConnection(socketChannel, TcpConnectionState.WAITING_FOR_GREETINGS, key.selector()));
        System.out.println("Add new connection: " + socketChannel.getRemoteAddress());
    }
}
