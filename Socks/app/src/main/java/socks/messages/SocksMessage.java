package socks.messages;

import socks.exceptions.WrongSocksMessageException;

public interface SocksMessage {
    byte[] toByteArray() throws WrongSocksMessageException;
}
