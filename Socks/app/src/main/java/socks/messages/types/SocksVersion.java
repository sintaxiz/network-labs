package socks.messages.types;

import socks.exceptions.WrongSocksMessageException;

public enum SocksVersion {
    SOCKS4,
    SOCKS5;

    public static SocksVersion parseByte(byte v) throws WrongSocksMessageException {
            switch (v) {
                case 0x04 -> {
                    return SOCKS4;
                }
                case 0x05 -> {
                    return SOCKS5;
                }
                default -> throw new WrongSocksMessageException();
            }
    }
}

