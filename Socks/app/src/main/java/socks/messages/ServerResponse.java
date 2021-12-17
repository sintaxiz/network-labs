package socks.messages;

import socks.messages.types.AuthMethod;
import socks.messages.types.ServerStatus;

public class ServerResponse implements SocksMessage {

    AuthMethod authMethod;
    ServerStatus serverStatus;

    public ServerResponse(AuthMethod authMethod, ServerStatus serverStatus) {
        this.authMethod = authMethod;
        this.serverStatus = serverStatus;
    }

    @Override
    public byte[] toByteArray() {
        byte []responseBytes = new byte[2];
        switch (serverStatus) {
            case SUCCESS -> {
                responseBytes[1] = 0x00;
            }
            case FAILURE -> {
                responseBytes[1] = 0x11;
            }
        }
        return responseBytes;
    }
}
