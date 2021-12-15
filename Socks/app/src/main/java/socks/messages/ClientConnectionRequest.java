package socks.messages;

import socks.messages.types.Command;
import socks.messages.types.SocksVersion;

public class ClientConnectionRequest {
    SocksVersion version;
    Command command;
    Socks5Address destinationAddr;
    int destinationPort;
}
