package socks.messages.types;

import socks.exceptions.WrongSocksMessageException;

public enum SocksCommand {
    ESTABLISH_TCP_CONNECTION,
    ESTABLISH_TCP_PORT_BINDING,
    ASSOCIATE_UDP_PORT;

    public static SocksCommand parseByte(byte c) throws WrongSocksMessageException {
        switch (c) {
            case 0x01 -> {
                return ESTABLISH_TCP_CONNECTION;
            }
            case 0x02 -> {
                return ESTABLISH_TCP_PORT_BINDING;
            }
            case 0x03 -> {
                return ASSOCIATE_UDP_PORT;
            }
            default -> throw new WrongSocksMessageException();
        }
    }
}
