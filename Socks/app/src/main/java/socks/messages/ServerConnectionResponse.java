package socks.messages;

import socks.exceptions.WrongSocksMessageException;
import socks.messages.types.ConnectionStatus;
import socks.messages.types.SocksVersion;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ServerConnectionResponse implements SocksMessage {
    SocksVersion socksVersion;
    ConnectionStatus connectionStatus;
    Socks5Address boundAddress;
    int boundPort;

    public ServerConnectionResponse(SocksVersion socksVersion, ConnectionStatus connectionStatus, Socks5Address boundAddress, int boundPort) {
        this.socksVersion = socksVersion;
        this.connectionStatus = connectionStatus;
        this.boundAddress = boundAddress;
        this.boundPort = boundPort;
    }

    @Override
    public byte[] toByteArray() throws WrongSocksMessageException {
        byte[] address = boundAddress.toBytes();
        int responseLength = 6 + address.length;
        byte[] bytes = new byte[responseLength];

        bytes[0] = socksVersion.toByte();
        bytes[1] = connectionStatus.toByte();
        bytes[2] = 0x00; // reserved byte
        bytes[3] = boundAddress.getAddressType().toByte();
        System.arraycopy(bytes, 4, address, 0, address.length);

        int portIdx = responseLength - 3;
        //destinationPort = ((msg[portIdx] & 0xff) << 8) | (msg[portIdx + 1] & 0xff);;     //port number in a network byte order
        bytes[portIdx] = (byte) (boundPort >> 8);
        bytes[portIdx + 1] = (byte) boundPort;
        return bytes;
    }
    // Response packet from server 	VER STATUS RSV BNDADDR BNDPORT

}
