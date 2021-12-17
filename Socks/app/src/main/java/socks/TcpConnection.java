package socks;

import socks.exceptions.TooShortSocksMessageException;
import socks.exceptions.WrongSocksMessageException;
import socks.messages.ClientCommandRequest;
import socks.messages.ClientGreeting;
import socks.messages.ServerAuthChoice;
import socks.messages.ServerResponse;
import socks.messages.types.AuthMethod;
import socks.messages.types.ServerStatus;
import socks.messages.types.SocksVersion;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class TcpConnection {
    SocketChannel inputChannel;
    SocketChannel outputChannel;
    byte[] messageToInput;
    ByteBuffer messageFromInput;
    byte[] tooShortMessage;
    TcpConnectionState currentState;

    ByteBuffer byteBuffer;

    // Selector associated with input/output channels
    Selector selector;

    public TcpConnection(SocketChannel inputChannel, TcpConnectionState currentState, Selector selector) {
        this.inputChannel = inputChannel;
        this.currentState = currentState;
        this.selector = selector;
        byteBuffer = ByteBuffer.allocate(1024);
        messageFromInput = ByteBuffer.allocate(1024);
    }

    private byte[] readData() throws IOException {
        int read = inputChannel.read(messageFromInput);
        messageFromInput.flip();
        if (read < 0) throw new IOException("end of channel");
        System.out.println("Read data from " + inputChannel.getRemoteAddress() + ": "
                + Arrays.toString(Arrays.copyOfRange(messageFromInput.array(), 0, read)));
        return Arrays.copyOfRange(messageFromInput.array(), 0, read);
    }


    public void readGreeting() throws IOException, WrongSocksMessageException {
        byte[] msg = readData();
        ClientGreeting clientGreeting = new ClientGreeting(msg);
        System.out.println("Get correct client greeting: " + clientGreeting);

        // Server chooses one of the methods (or sends a failure response if none of them are acceptable).
        ServerAuthChoice serverAuthChoice = new ServerAuthChoice(SocksVersion.SOCKS5, AuthMethod.NO_AUTH);

        // add message, later this message will write to inputChannel
        messageToInput = serverAuthChoice.toByteArray();
        inputChannel.register(selector, SelectionKey.OP_WRITE);
        currentState = TcpConnectionState.WAITING_FOR_COMMAND;
    }

    public void readCommandRequest() throws IOException, WrongSocksMessageException {
        System.out.println("Going to read command request...");
        byte[] msg = readData();
        if (tooShortMessage != null) {
            msg = concatenate(tooShortMessage, msg);
            tooShortMessage = null;
        }
        ClientCommandRequest clientCommandRequest = null;
        try {
            clientCommandRequest = new ClientCommandRequest(msg);
            System.out.println("Read command request: " + clientCommandRequest);
            messageToInput = new byte[] {0x06, 0x06};
            inputChannel.register(selector, SelectionKey.OP_WRITE);
            switch(clientCommandRequest.getSocksCommand()) {
                case ESTABLISH_TCP_CONNECTION -> {
                    // TODO: establish connection with dstaddr
                }
                default -> throw new UnsupportedOperationException();
            }
        } catch (TooShortSocksMessageException e) {
            System.out.println("Not full message. Waiting for full message...");
            tooShortMessage = msg;
        }

    }

    public void write(SocketChannel socketChannel) throws IOException {
        if (socketChannel.equals(inputChannel)) {
            if (messageToInput != null) {
                byteBuffer.put(messageToInput);
                byteBuffer.flip();
                writeToChannel(socketChannel, byteBuffer);
                System.out.println("Successfully write to input channel: " + Arrays.toString(messageToInput));
                byteBuffer.clear();
                System.out.println("message to input == " + Arrays.toString(messageToInput));
                messageToInput = null;
            }

        } else if (socketChannel.equals(outputChannel)) {
            System.out.println("Wants to write to out channel...");
        } else {
            System.out.println("ERROR: wrong socket channel (not input, not output)");
        }
    }

    private void writeToChannel(SocketChannel channel, ByteBuffer msg) throws IOException {
        channel.write(msg);
    }

    public void readAuthRequest() throws IOException {
        System.out.println("Going to read auth request...");
        byte[] msg = readData();         // actually ignoring it now
        System.out.println("Successfully read auth request!");

        ServerResponse serverResponse = new ServerResponse(AuthMethod.NO_AUTH, ServerStatus.SUCCESS);
        messageToInput = serverResponse.toByteArray();
        //inputChannel.register(selector, SelectionKey.OP_WRITE);
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
}
