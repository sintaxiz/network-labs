package socks;

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
        socketChannel.register(key.selector(), SelectionKey.OP_READ);
        System.out.println("Add new connection: " + socketChannel.getRemoteAddress());
    }
}
