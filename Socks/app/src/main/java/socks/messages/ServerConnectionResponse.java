package socks.messages;

import socks.messages.types.ConnectionStatus;
import socks.messages.types.SocksVersion;

public class ServerConnectionResponse {
    SocksVersion socksVersion;
    ConnectionStatus connectionStatus;
    Socks5Address boundAddress;
    int boundPort;
    // Response packet from server 	VER STATUS RSV BNDADDR BNDPORT

}
