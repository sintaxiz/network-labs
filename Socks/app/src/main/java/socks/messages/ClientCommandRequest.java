package socks.messages;

import socks.exceptions.WrongSocksMessageException;
import socks.messages.types.SocksCommand;
import socks.messages.types.SocksVersion;

public class ClientCommandRequest {

    SocksVersion version;
    SocksCommand socksCommand;
    Socks5Address destinationAddr;
    int destinationPort;

    public ClientCommandRequest(byte[] msg) throws WrongSocksMessageException {
        // Client sends a connection request
        // VER (1)	CMD (1)	RSV (1)	DSTADDR (var) DSTPORT (2)
        version = SocksVersion.parseByte(msg[0]);
        socksCommand = SocksCommand.parseByte(msg[1]);

        // TODO: rsv == 0x00 reserved byte
        // TODO: parse dstaddr

        destinationPort = msg[3] | msg[4] << 8;     //port number in a network byte order
    }

    public SocksVersion getVersion() {
        return version;
    }

    public SocksCommand getSocksCommand() {
        return socksCommand;
    }

    public Socks5Address getDestinationAddr() {
        return destinationAddr;
    }

    public int getDestinationPort() {
        return destinationPort;
    }
}
