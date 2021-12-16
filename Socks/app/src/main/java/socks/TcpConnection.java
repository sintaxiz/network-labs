package socks;

import org.checkerframework.checker.units.qual.C;
import socks.exceptions.WrongSocksMessageException;
import socks.messages.ClientCommandRequest;
import socks.messages.ClientGreeting;
import socks.messages.types.SocksCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class TcpConnection {
    SocketChannel inputChannel;
    SocketChannel outputChannel;
    ByteBuffer exchangeBuffer;
    TcpConnectionState currentState;

    // Selector associated with input/output channels
    Selector selector;

    public TcpConnection(SocketChannel inputChannel, TcpConnectionState currentState, Selector selector) {
        this.inputChannel = inputChannel;
        this.currentState = currentState;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
    }

    private byte[] readData() throws IOException {
        int read = inputChannel.read(exchangeBuffer);
        System.out.println("Read data from " + inputChannel.getRemoteAddress() + ": "
                + new String(exchangeBuffer.array(), 0, read));
        return Arrays.copyOfRange(exchangeBuffer.array(), 0, read);
    }


    public void readGreeting() throws IOException, WrongSocksMessageException {
        byte[] msg = readData();
        ClientGreeting clientGreeting = new ClientGreeting(msg);

        // Server chooses one of the methods (or sends a failure response if none of them are acceptable).
        // TODO: there need to send 0x05 0x00
        inputChannel.register(selector, SelectionKey.OP_WRITE);

        //connectedClients.add(socketChannel);
    }

    public void readCommandRequest() throws IOException, WrongSocksMessageException {
        byte[] msg = readData();
        ClientCommandRequest clientCommandRequest = new ClientCommandRequest(msg);
        switch(clientCommandRequest.getSocksCommand()) {
            case ESTABLISH_TCP_CONNECTION -> {
                // TODO: establish connection with dstaddr
            }
            default -> throw new UnsupportedOperationException();
        }

    }
}
