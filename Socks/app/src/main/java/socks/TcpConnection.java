package socks;

import socks.exceptions.TooShortSocksMessageException;
import socks.exceptions.WrongSocksMessageException;
import socks.messages.*;
import socks.messages.types.AuthMethod;
import socks.messages.types.ServerStatus;
import socks.messages.types.SocksVersion;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class TcpConnection {
    SocketChannel clientChannel;
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
        messageFromInput.flip();
        if (read < 0) throw new IOException("end of channel");
        System.out.println("Read data from " + channel.getRemoteAddress() + ": "
                + Arrays.toString(Arrays.copyOfRange(messageFromInput.array(), 0, read)));
        return Arrays.copyOfRange(messageFromInput.array(), 0, read);
    }


    public void readGreeting() throws IOException, WrongSocksMessageException {
        byte[] msg = readData(clientChannel);
        ClientGreeting clientGreeting = new ClientGreeting(msg);
        System.out.println("Get correct client greeting: " + clientGreeting);

        // Server chooses one of the methods (or sends a failure response if none of them are acceptable).
        ServerAuthChoice serverAuthChoice = new ServerAuthChoice(SocksVersion.SOCKS5, AuthMethod.NO_AUTH);

        // add message, later this message will write to inputChannel
        messageToClient = serverAuthChoice.toByteArray();
        clientChannel.register(selector, SelectionKey.OP_WRITE);
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
                    establishTcpConnection(command.getDestinationAddr(), command.getDestinationPort());
                    currentState = TcpConnectionState.TRANSMITTING_DATA;
                }
                default -> throw new UnsupportedOperationException();
            }
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (TooShortSocksMessageException e) {
            System.out.println("Not full command message. Waiting for full message...");
            tooShortMessage = msg;
        }

    }

    private void establishTcpConnection(Socks5Address destinationAddr, int destinationPort) throws IOException {
        serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.connect(new InetSocketAddress(destinationAddr.getAddress(), destinationPort));
        System.out.println("Successfully connect to " + destinationAddr.getAddress() +":" + destinationPort + "!");
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

    public void write(SocketChannel socketChannel) throws IOException {
        System.out.println("going write to " + socketChannel.getRemoteAddress());
        if (socketChannel.equals(clientChannel)) {
            if (messageToClient != null) {
                byteBuffer.put(messageToClient);
                byteBuffer.flip();
                writeToChannel(socketChannel, byteBuffer);
                System.out.println("Successfully write to input channel: " + Arrays.toString(messageToClient));
                byteBuffer.clear();
                System.out.println("message to input == " + Arrays.toString(messageToClient));
                messageToClient = null;
            }
        } else if (socketChannel.equals(serverChannel)) {
            System.out.println("Wants to write to out channel...");
        } else {
            System.out.println("ERROR: wrong socket channel (not input, not output)");
        }
    }

    public void read(SocketChannel socketChannel) throws IOException {
        if (socketChannel.equals(clientChannel)) {
            System.out.println("Going to read from client...");
            messageToServer = readData(clientChannel);
            serverChannel.register(selector, SelectionKey.OP_WRITE, this);
        } else if (socketChannel.equals(serverChannel)) {
            System.out.println("Going to read from server...");
            messageToClient = readData(serverChannel);
            clientChannel.register(selector, SelectionKey.OP_WRITE, this);
        } else {
            System.out.println("ERROR: wrong socket channel (not input, not output)");
        }
    }
}
