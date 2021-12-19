package socks;

public enum TcpConnectionState {
    WAITING_FOR_GREETINGS,
    WAITING_FOR_COMMAND,
    TRANSMITTING_DATA
}
