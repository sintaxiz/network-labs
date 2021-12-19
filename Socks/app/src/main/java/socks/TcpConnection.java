package socks;

import socks.exceptions.TooShortSocksMessageException;
import socks.exceptions.WrongSocksMessageException;
import socks.messages.*;
import socks.messages.types.AuthMethod;
import socks.messages.types.ConnectionStatus;
import socks.messages.types.ServerStatus;
import socks.messages.types.SocksVersion;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class TcpConnection {
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

    public TcpConnection(SocketChannel clientChannel, TcpConnectionState currentState, Selector selector) {
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
        System.out.println("Read data from " + channel.getRemoteAddress() + ": "
                + Arrays.toString(Arrays.copyOfRange(messageFromInput.array(), 0, totalRead)));
        return Arrays.copyOfRange(messageFromInput.array(), 0, totalRead);
    }


    public void readGreeting() throws IOException, WrongSocksMessageException {
        byte[] msg = readData(clientChannel);
        ClientGreeting clientGreeting = new ClientGreeting(msg);
        System.out.println("Get correct client greeting: " + clientGreeting);

        // Server chooses one of the methods (or sends a failure response if none of them are acceptable).
        ServerAuthChoice serverAuthChoice = new ServerAuthChoice(SocksVersion.SOCKS5, AuthMethod.NO_AUTH);

        // add message, later this message will write to inputChannel
        messageToClient = serverAuthChoice.toByteArray();
        clientChannel.register(selector, SelectionKey.OP_WRITE, this);
        currentState = TcpConnectionState.WAITING_FOR_COMMAND;
    }

    public void readCommandRequest(SelectionKey key) throws IOException, WrongSocksMessageException {
        System.out.println("Going to read command request...");
        byte[] msg = readData(clientChannel);
        if (tooShortMessage != null) {
            msg = concatenate(tooShortMessage, msg);
            tooShortMessage = null;
        }
        ClientCommandRequest command = null;
        try {
            command = new ClientCommandRequest(msg);
            System.out.println("Read command request: " + command);
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
            System.out.println("Not full command message. Waiting for full message...");
            tooShortMessage = msg;
        }

    }

    private void queueClientMessage(SocksMessage message) throws ClosedChannelException, WrongSocksMessageException {
        messageToClient = message.toByteArray();
    }

    private void startTcpConnection(Socks5Address destinationAddr, int destinationPort) throws IOException {
        serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.connect(new InetSocketAddress(destinationAddr.getAddress(), destinationPort));
        serverChannel.register(selector, SelectionKey.OP_CONNECT, this);
    }

    public void finishTcpConnection(SocketChannel socketChannel, SelectionKey key) throws IOException, WrongSocksMessageException {
        ServerConnectionResponse response;
        if (socketChannel.finishConnect()) {
            System.out.println("Successfully connect!");
            response = new ServerConnectionResponse(
                    SocksVersion.SOCKS5,
                    ConnectionStatus.SUCCEEDED,
                    serverAddress,
                    serverPort);
            currentState = TcpConnectionState.TRANSMITTING_DATA;
        } else {
            System.out.println("Can not finish connection...");
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

    public void readAuthRequest() throws IOException {
        System.out.println("Going to read auth request...");
        byte[] msg = readData(clientChannel);         // actually ignoring it now
        System.out.println("Successfully read auth request!");

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
        System.out.println("going write to " + socketChannel.getRemoteAddress());
        if (socketChannel.equals(clientChannel)) {
            if (messageToClient != null) {
                writeToChannel(socketChannel, ByteBuffer.wrap(messageToClient));
                System.out.println("Successfully write to client channel: " + Arrays.toString(messageToClient));
                messageToClient = null;
                key.interestOps(SelectionKey.OP_READ);
            } else {
                System.out.println("no message to client..");
                // not interesting in write because no message
                key.cancel();
            }
        } else if (socketChannel.equals(serverChannel)) {
            System.out.println("Wants to write to server channel...");
            if (messageToServer != null && messageToServer.length > 0) {
                byteBuffer.put(messageToServer);
                byteBuffer.flip();
                writeToChannel(socketChannel, byteBuffer);
                System.out.println("Successfully write to server channel: " + Arrays.toString(messageToServer));
                byteBuffer.clear();
                messageToServer = null;
                key.interestOps(SelectionKey.OP_READ);
            } else {
                key.cancel();
            }
        } else {
            System.out.println("ERROR: wrong socket channel (not input, not output)");
        }
    }

    public void read(SocketChannel socketChannel, SelectionKey key) throws IOException {
        if (socketChannel.equals(clientChannel)) {
            System.out.println("Going to read from client...");
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
            //System.out.println("successfully register server channel: " + serverChannel.getRemoteAddress());
        } else if (socketChannel.equals(serverChannel)) {
            System.out.println("Going to read from server...");
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
            System.out.println("ERROR: wrong socket channel (not input, not output)");
        }
    }
}
