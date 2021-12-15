package socks.messages.types;

public enum ConnectionStatus {
    SUCCEEDED,
    SERVER_FAILURE,
    NOT_ALLOWED,
    NET_UNREACHABLE,
    HOST_UNREACHABLE,
    REFUSED,
    TTL_EXPIRED,
    NOT_SUPPORTED_CMD,
    NOT_SUPPORTED_ADDR
}
