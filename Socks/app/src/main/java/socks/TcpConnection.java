package socks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import socks.exceptions.TooShortSocksMessageException;
import socks.exceptions.WrongSocksMessageException;
import socks.messages.*;
import socks.messages.types.AuthMethod;
import socks.messages.types.ConnectionStatus;
import socks.messages.types.ServerStatus;
import socks.messages.types.SocksVersion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class TcpConnection implements DnsSubscriber {
    private static Logger log = LogManager.getLogger(TcpConnection.class);

    private final DnsResolver dnsResolver;

    SocketChannel clientChannel;

    int serverPort;
    Socks5Address serverAddress;
    SocketChannel serverChannel;


    byte[] messageToClient;
    byte[] messageToServer;
    ByteBuffer messageFromInput;
    byte[] tooShortMessage;
    TcpConnectionState currentState;

    ByteBuffer byteBuffer;

    // Selector associated with input/output channels
    Selector selector;

    public TcpConnection(DnsResolver dnsResolver, SocketChannel clientChannel, TcpConnectionState currentState, Selector selector) {
        this.dnsResolver = dnsResolver;
        this.clientChannel = clientChannel;
        this.currentState = currentState;
        this.selector = selector;
        byteBuffer = ByteBuffer.allocate(1024);
        messageFromInput = ByteBuffer.allocate(1024);
    }

    private byte[] readData(SocketChannel channel) throws IOException {
        int read = channel.read(messageFromInput);
        int totalRead = read;
        while (read > 0) {
            read = channel.read(messageFromInput);
            totalRead += read;
        }
        messageFromInput.flip();
        if (totalRead < 0) throw new IOException("end of channel");
        log.debug("Read data from " + channel.getRemoteAddress() + ": "
                + Arrays.toString(Arrays.copyOfRange(messageFromInput.array(), 0, totalRead)));
        return Arrays.copyOfRange(messageFromInput.array(), 0, totalRead);
    }


    public void readGreeting() throws IOException, WrongSocksMessageException {
        byte[] msg = readData(clientChannel);
        ClientGreeting clientGreeting = new ClientGreeting(msg);
        log.debug("Get correct client greeting: " + clientGreeting);

        // Server chooses one of the methods (or sends a failure response if none of them are acceptable).
        ServerAuthChoice serverAuthChoice = new ServerAuthChoice(SocksVersion.SOCKS5, AuthMethod.NO_AUTH);

        // add message, later this message will write to inputChannel
        messageToClient = serverAuthChoice.toByteArray();
        clientChannel.register(selector, SelectionKey.OP_WRITE, this);
        currentState = TcpConnectionState.WAITING_FOR_COMMAND;
    }

    private void readCommandRequest(SelectionKey key) throws IOException, WrongSocksMessageException {
        log.debug("Going to read command request...");
        byte[] msg = readData(clientChannel);
        if (tooShortMessage != null) {
            msg = concatenate(tooShortMessage, msg);
            tooShortMessage = null;
        }
        ClientCommandRequest command = null;
        try {
            command = new ClientCommandRequest(msg);
            log.debug("Read command request: " + command);
            switch(command.getSocksCommand()) {
                case ESTABLISH_TCP_CONNECTION -> {
                    startTcpConnection(command.getDestinationAddr(), command.getDestinationPort());
                    serverAddress = command.getDestinationAddr();
                    serverPort = command.getDestinationPort();
                    currentState = TcpConnectionState.CONNECTING;
                }
                default -> throw new UnsupportedOperationException();
            }
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (TooShortSocksMessageException e) {
            log.debug("Not full command message. Waiting for full message...");
            tooShortMessage = msg;
        }

    }

    private void queueClientMessage(SocksMessage message) throws ClosedChannelException, WrongSocksMessageException {
        messageToClient = message.toByteArray();
    }

    private void startTcpConnection(Socks5Address destinationAddr, int destinationPort) throws IOException {
        serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);
        switch (destinationAddr.getAddressType()) {
            case IPv4, IPv6 -> {
                serverChannel.connect(new InetSocketAddress(destinationAddr.getAddress(), destinationPort));
                serverChannel.register(selector, SelectionKey.OP_CONNECT, this);
            }
            case DOMAIN_NAME -> {
                serverChannel.connect(
                        new InetSocketAddress(dnsResolver.resolve(destinationAddr.getAddress()),
                                                destinationPort)
                );
                //dnsResolver.subscribeForResolving(this);
                //serverChannel.connect(new InetSocketAddress(, destinationPort));
            }
        }
    }

    void finishTcpConnection(SocketChannel socketChannel, SelectionKey key) throws IOException, WrongSocksMessageException {
        ServerConnectionResponse response;
        if (socketChannel.finishConnect()) {
            log.debug("Successfully connect!");
            response = new ServerConnectionResponse(
                    SocksVersion.SOCKS5,
                    ConnectionStatus.SUCCEEDED,
                    serverAddress,
                    serverPort);
            currentState = TcpConnectionState.TRANSMITTING_DATA;
        } else {
            log.debug("Can not finish connection...");
            response = new ServerConnectionResponse(
                    SocksVersion.SOCKS5,
                    ConnectionStatus.HOST_UNREACHABLE,
                    serverAddress,
                    serverPort
            );
            currentState = TcpConnectionState.WAITING_FOR_COMMAND;
        }
        queueClientMessage(response);
        clientChannel.register(selector, SelectionKey.OP_WRITE, this);
    }


    private void writeToChannel(SocketChannel channel, ByteBuffer msg) throws IOException {
        channel.write(msg);
    }

    private void readAuthRequest() throws IOException {
        log.debug("Going to read auth request...");
        byte[] msg = readData(clientChannel);         // actually ignoring it now
        log.debug("Successfully read auth request!");

        ServerResponse serverResponse = new ServerResponse(AuthMethod.NO_AUTH, ServerStatus.SUCCESS);
        messageToClient = serverResponse.toByteArray();
        currentState = TcpConnectionState.WAITING_FOR_COMMAND;
    }

    private byte[] concatenate(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;

        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public void write(SocketChannel socketChannel, SelectionKey key) throws IOException {
        log.debug("going write to " + socketChannel.getRemoteAddress());
        if (socketChannel.equals(clientChannel)) {
            if (messageToClient != null) {
                writeToChannel(socketChannel, ByteBuffer.wrap(messageToClient));
                log.debug("Successfully write to client channel: " + Arrays.toString(messageToClient));
                messageToClient = null;
                key.interestOps(SelectionKey.OP_READ);
            } else {
                log.debug("no message to client..");
                // not interesting in write because no message
                key.cancel();
            }
        } else if (socketChannel.equals(serverChannel)) {
            log.debug("Wants to write to server channel...");
            if (messageToServer != null && messageToServer.length > 0) {
                byteBuffer.put(messageToServer);
                byteBuffer.flip();
                writeToChannel(socketChannel, byteBuffer);
                log.debug("Successfully write to server channel: " + Arrays.toString(messageToServer));
                byteBuffer.clear();
                messageToServer = null;
                key.interestOps(SelectionKey.OP_READ);
            } else {
                key.cancel();
            }
        } else {
            log.debug("ERROR: wrong socket channel (not input, not output)");
        }
    }

    public void read(SocketChannel socketChannel, SelectionKey key) throws IOException, WrongSocksMessageException {
        switch (currentState) {
            case WAITING_FOR_GREETINGS -> readGreeting();
            case WAITING_FOR_COMMAND -> readCommandRequest(key);
            case TRANSMITTING_DATA -> transmitData(socketChannel, key);
            case CONNECTING -> key.interestOps(0);
        }
    }

    private void transmitData(SocketChannel socketChannel, SelectionKey key) throws IOException {
        if (socketChannel.equals(clientChannel)) {
            log.debug("Going to read from client...");
            if (messageToServer != null && messageToServer.length > 0) {
                messageToServer = concatenate(messageToServer, readData(clientChannel));
            } else {
                messageToServer = readData(clientChannel);
            }
            if (messageToServer.length != 0) {
                serverChannel.register(selector, SelectionKey.OP_WRITE, this);
            } else {
                key.cancel();
            }
            //log.debug("successfully register server channel: " + serverChannel.getRemoteAddress());
        } else if (socketChannel.equals(serverChannel)) {
            log.debug("Going to read from server...");
            if (messageToClient != null && messageToClient.length > 0) {
                messageToClient = concatenate(messageToClient, readData(serverChannel));
            } else {
                messageToClient = readData(serverChannel);
            }
            if (messageToClient.length != 0) {
                clientChannel.register(selector, SelectionKey.OP_WRITE, this);
            } else {
                key.cancel();
            }
        } else {
            log.debug("ERROR: wrong socket channel (not input, not output)");
        }
    }


    public void close() throws IOException {
        if (clientChannel != null) {
            clientChannel.close();
        }
        if (serverChannel != null) {
            serverChannel.close();
        }
    }
}
