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

    public static byte toByte(SocksVersion socksVersion) {
        switch (socksVersion) {
            case SOCKS4 -> {
                return 0x04;
            }
            case SOCKS5 -> {
                return 0x05;
            }
            default -> throw new RuntimeException("Wrong value of enum");
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case SOCKS4 -> {
                return "SOCKSv4";
            }
            case SOCKS5 -> {
                return "SOCKSv5";
            }
            default -> throw new RuntimeException("Wrong value of enum");
        }
    }
}

