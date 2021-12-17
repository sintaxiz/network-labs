package socks.messages.types;

public enum AuthMethod {
    NO_AUTH;

    public static byte toByte(AuthMethod chosenMethod) {
        return 0x00;
    }
}
