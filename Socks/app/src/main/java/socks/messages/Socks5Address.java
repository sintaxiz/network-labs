package socks.messages;

import socks.exceptions.TooShortSocksMessageException;
import socks.exceptions.WrongSocksMessageException;
import socks.messages.types.Socks5AddressType;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Socks5Address {
    Socks5AddressType addressType;
    String address;

    public Socks5Address(Socks5AddressType addressType, String address) {
        this.addressType = addressType;
        this.address = address;
    }

    public static Socks5Address parseBytes(byte[] bytes, Socks5AddressType addressType) throws WrongSocksMessageException, TooShortSocksMessageException {
        switch (addressType) {
            case IPv4 -> {
                if (bytes.length < 5) throw new TooShortSocksMessageException();
                return new Socks5Address(Socks5AddressType.IPv4,
                        Byte.toUnsignedInt(bytes[0]) + "." + Byte.toUnsignedInt(bytes[1]) + "." + Byte.toUnsignedInt(bytes[2]) + "." + Byte.toUnsignedInt(bytes[3]));
            }
            case IPv6 -> {
                return new Socks5Address(Socks5AddressType.IPv6, ""); // maybe add ipv6 support later
            }
            case DOMAIN_NAME -> {
                if (bytes.length < 1)  throw new TooShortSocksMessageException();
                int domainNameLength = bytes[0];
                if (bytes.length < domainNameLength) throw new TooShortSocksMessageException();
                byte[] domainNameBytes = Arrays.copyOfRange(bytes, 1, domainNameLength + 1);
                return new Socks5Address(Socks5AddressType.DOMAIN_NAME, new String(domainNameBytes, StandardCharsets.UTF_8));
            }
            default -> throw new WrongSocksMessageException("bad value for enum Socks5AddressType");
        }
    }

    public Socks5AddressType getAddressType() {
        return addressType;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Socks5Address{" +
                "addressType=" + addressType +
                ", address='" + address + '\'' +
                '}';
    }
}
