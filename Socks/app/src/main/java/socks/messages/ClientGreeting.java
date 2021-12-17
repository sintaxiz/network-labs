package socks.messages;

import socks.exceptions.WrongSocksMessageException;
import socks.messages.types.AuthMethod;
import socks.messages.types.SocksVersion;

import java.util.HashSet;
import java.util.Set;

/**
 * Client connects and sends a greeting, which includes a list of authentication methods supported.
 */
public class ClientGreeting implements SocksMessage {
    SocksVersion socksVersion;
    int numberAuth;
    Set<AuthMethod> authMethods;

    /**
     * parse byte array
     * @param msg client greeting in format:
     * @throws WrongSocksMessageException if some of bytes are incorrect
     */
    public ClientGreeting(byte[] msg) throws WrongSocksMessageException {
        if (msg.length < 3) {
            throw new WrongSocksMessageException("too short message");
        }
        switch (msg[0]) {
            case 0x04 -> socksVersion = SocksVersion.SOCKS4;
            case 0x05 -> socksVersion = SocksVersion.SOCKS5;
            default -> throw new WrongSocksMessageException();
        }
        socksVersion = SocksVersion.parseByte(msg[0]);
        numberAuth = msg[1];
        if (numberAuth < 0) {
            throw new WrongSocksMessageException();
        }
        authMethods = new HashSet<>();
        switch (msg[2]) {
            case 0x00 -> authMethods.add(AuthMethod.NO_AUTH);
            default -> throw new WrongSocksMessageException();
        }
    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "Socks Version: " + socksVersion.toString() + " Number of auth methods: " + numberAuth;
    }
}
