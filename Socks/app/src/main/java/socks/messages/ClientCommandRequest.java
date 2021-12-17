package socks.messages;

import socks.exceptions.TooShortSocksMessageException;
import socks.exceptions.WrongSocksMessageException;
import socks.messages.types.Socks5AddressType;
import socks.messages.types.SocksCommand;
import socks.messages.types.SocksVersion;

import java.util.Arrays;

public class ClientCommandRequest implements SocksMessage {

    SocksVersion version;
    SocksCommand socksCommand;
    Socks5Address destinationAddr;
    int destinationPort;

    public ClientCommandRequest(byte[] msg) throws WrongSocksMessageException, TooShortSocksMessageException {
        if (msg.length < 4) {
            throw new TooShortSocksMessageException();
        }
        // Client sends a connection request
        // VER (1)	CMD (1)	RSV (1)	DSTADDR (var) DSTPORT (2)
        version = SocksVersion.parseByte(msg[0]);
        socksCommand = SocksCommand.parseByte(msg[1]);
        if (msg[2] != 0x00) {
            throw new WrongSocksMessageException("reserved byte should be 0x00");
        }
        System.out.println("byte for address type: " + msg[3]);
        Socks5AddressType addressType = Socks5AddressType.parseByte(msg[3]);
        destinationAddr = Socks5Address.parseBytes( Arrays.copyOfRange(msg, 4, msg.length), addressType);
        int portIdx = 0;
        switch (addressType) {
            case IPv4 -> portIdx = 4 + 3;
            case IPv6 -> portIdx = 16 + 3;
            case DOMAIN_NAME -> portIdx = msg[4] + 5;
        }
        if (portIdx >= msg.length) throw new TooShortSocksMessageException();
        destinationPort = ((msg[portIdx] & 0xff) << 8) | (msg[portIdx + 1] & 0xff);;     //port number in a network byte order
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

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "Socks version: " + version + ", command: " + socksCommand +
                ", destination address: " + destinationAddr + " destination port: " + destinationPort;
    }
}
