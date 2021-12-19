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
    NOT_SUPPORTED_ADDR;

    public byte toByte() {
        switch (this) {
            case SUCCEEDED -> {
                return 0x00;
            }
            case SERVER_FAILURE -> {
                return 0x01;
            }
            case NOT_ALLOWED -> {
                return 0x02;
            }
            case NET_UNREACHABLE -> {
                return 0x03;
            }
            case HOST_UNREACHABLE -> {
                return 0x04;
            }
            case REFUSED -> {
                return 0x05;
            }
            case TTL_EXPIRED -> {
                return 0x06;
            }
            case NOT_SUPPORTED_CMD -> {
                return 0x07;
            }
            case NOT_SUPPORTED_ADDR -> {
                return 0x08;
            }
            default -> {
                return 0x09;
            }
        }
    }
}
