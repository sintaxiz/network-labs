package socks;

import socks.exceptions.WrongSocksMessageException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

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
            selector.select(this::performActionOnKey);
            //System.out.println("DEBUG: " + selector.keys());
        }

        selector.close();
    }

    private void performActionOnKey(SelectionKey key) {
        try {
            if (!key.isValid()) {
                System.out.println("SELECT ERROR: not valid key");
            } else if (key.isAcceptable()) {
                accept(key);
            } else if (key.isReadable()) {
                read(key);
            } else if (key.isWritable()) {
                write(key);
            } else if (key.isConnectable()) {
                connect(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect(SelectionKey key) throws IOException {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            System.out.println("SELECT OP_CONNECT:" + socketChannel.getRemoteAddress());
            TcpConnection tcpConnection = (TcpConnection) key.attachment();
            tcpConnection.finishTcpConnection(socketChannel, key);
        } catch (WrongSocksMessageException e) {
            System.out.println("ERROR SELECT OP_CONNECT");
        }

    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        System.out.println("SELECT OP_WRITE:" + socketChannel.getRemoteAddress());
        try {
            TcpConnection tcpConnection = (TcpConnection) key.attachment();
            tcpConnection.write(socketChannel, key);
            //socketChannel.register(key.selector(), SelectionKey.OP_READ, tcpConnection);
        } catch (IOException e) {
            e.printStackTrace();
            key.cancel();
            socketChannel.close();
        }
        //key.interestOps(SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        System.out.println("SELECT OP_READ:" + socketChannel.getRemoteAddress());
        try {
            TcpConnection tcpConnection = (TcpConnection) key.attachment();
            switch (tcpConnection.currentState) {
                case WAITING_FOR_GREETINGS -> tcpConnection.readGreeting();
                case WAITING_FOR_COMMAND -> tcpConnection.readCommandRequest(key);
                case TRANSMITTING_DATA -> tcpConnection.read(socketChannel);
                case CONNECTING -> key.interestOps(0);
            }
            //key.interestOps(SelectionKey.OP_WRITE);
            key.attach(tcpConnection);
        } catch (IOException | WrongSocksMessageException e) {
            e.printStackTrace();
            key.cancel();
        }
    }

    // Добавляет нового клиента
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        System.out.println("SELECT OP_ACCEPT:" + serverSocketChannel.getLocalAddress());
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ,
                new TcpConnection(socketChannel, TcpConnectionState.WAITING_FOR_GREETINGS, key.selector()));
        System.out.println("Add new connection: " + socketChannel.getRemoteAddress());
        System.out.println("Waiting a greeting from: " + socketChannel.getRemoteAddress());
    }
}
