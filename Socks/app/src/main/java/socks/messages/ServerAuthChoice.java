package socks.messages;

import socks.messages.types.AuthMethod;
import socks.messages.types.SocksVersion;

public class ServerAuthChoice {
    SocksVersion version;
    AuthMethod chosenMethod;
}
