package socks.messages.types;

import socks.exceptions.WrongSocksMessageException;

public enum Socks5AddressType {
    IPv4, IPv6, DOMAIN_NAME;

    public static Socks5AddressType parseByte(byte b) throws WrongSocksMessageException {
        switch (b) {
            case 0x01 -> {
                return IPv4;
            }
            case 0x03 -> {
                return DOMAIN_NAME;
            }
            case 0x04 -> {
                return IPv6;
            }
            default -> throw new WrongSocksMessageException("bad value for address type");
        }
    }


}
