package socks.messages;

import socks.messages.types.AuthMethod;
import socks.messages.types.SocksVersion;

public class ServerAuthChoice implements SocksMessage {
    SocksVersion version;
    AuthMethod chosenMethod;

    public ServerAuthChoice(SocksVersion version, AuthMethod chosenMethod) {
        this.version = version;
        this.chosenMethod = chosenMethod;
    }

    @Override
    public byte[] toByteArray() {
        byte[] authChoiceBytes = new byte[2];
        authChoiceBytes[0] = SocksVersion.toByte(version);
        authChoiceBytes[1] = AuthMethod.toByte(chosenMethod);
        return authChoiceBytes;
    }
}
