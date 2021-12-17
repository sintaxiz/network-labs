package socks.messages;

import socks.messages.types.ConnectionStatus;
import socks.messages.types.SocksVersion;

public class ServerConnectionResponse implements SocksMessage {
    SocksVersion socksVersion;
    ConnectionStatus connectionStatus;
    Socks5Address boundAddress;
    int boundPort;

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }
    // Response packet from server 	VER STATUS RSV BNDADDR BNDPORT

}
