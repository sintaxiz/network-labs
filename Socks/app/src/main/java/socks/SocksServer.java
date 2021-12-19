package socks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import socks.exceptions.WrongSocksMessageException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;

public class SocksServer {
    private static Logger log = LogManager.getLogger(SocksServer.class);

    private final int port;
    public static String SERVER_ADDRESS = "127.0.0.1";
    private DatagramChannel dnsChannel;

    public SocksServer(int port) {
        this.port = port;
    }

    public void serveConnections() throws IOException {
        log.debug("Creating server socket");

        final ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.bind(new InetSocketAddress(SERVER_ADDRESS, port));
        final Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        dnsChannel = DatagramChannel.open();
        dnsChannel.configureBlocking(false);

        log.debug("Start serving");
        while (!Thread.currentThread().isInterrupted()) {
            selector.select(this::performActionOnKey);
            //log.debug("DEBUG: " + selector.keys());
        }

        selector.close();
    }

    private void performActionOnKey(SelectionKey key) {
        try {
            if (!key.isValid()) {
                log.debug("SELECT ERROR: not valid key");
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
            log.error("Cannot perform action on key: " + key + ". Reason: " + e.getMessage());
        }
    }

    private void connect(SelectionKey key) throws IOException {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            log.debug("SELECT OP_CONNECT:" + socketChannel.getRemoteAddress());
            TcpConnection tcpConnection = (TcpConnection) key.attachment();
            tcpConnection.finishTcpConnection(socketChannel, key);
        } catch (WrongSocksMessageException e) {
            log.debug("ERROR SELECT OP_CONNECT");
        }

    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        log.debug("SELECT OP_WRITE:" + socketChannel.getRemoteAddress());
        TcpConnection tcpConnection = (TcpConnection) key.attachment();
        try {
            tcpConnection.write(socketChannel, key);
        } catch (IOException e) {
            log.error("Cannot perform OP_WRITE on key " + key + ". Reason: " + e.getMessage()
                    + ". This socket channel will be closed.");
            key.cancel();
            tcpConnection.close();
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        log.debug("SELECT OP_READ:" + socketChannel.getRemoteAddress());
        TcpConnection tcpConnection = (TcpConnection) key.attachment();
        try {
            tcpConnection.read(socketChannel, key);
        } catch (IOException | WrongSocksMessageException e) {
            log.error("Cannot perform OP_READ on key " + key + ". Reason: " + e.getMessage()
                    + ". This socket channel will be closed");
            key.cancel();
            tcpConnection.close();
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        log.debug("SELECT OP_ACCEPT:" + serverSocketChannel.getLocalAddress());
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(key.selector(), SelectionKey.OP_READ,
                new TcpConnection(new DnsResolver(key.selector(), dnsChannel), socketChannel,
                        TcpConnectionState.WAITING_FOR_GREETINGS, key.selector()));
        log.debug("Add new connection: " + socketChannel.getRemoteAddress());
        log.debug("Waiting a greeting from: " + socketChannel.getRemoteAddress());
    }
}
