package socks.exceptions;

public class WrongSocksMessageException extends Throwable {
    public WrongSocksMessageException() {

    }
    public WrongSocksMessageException(String s) {
        super(s);
    }
}
