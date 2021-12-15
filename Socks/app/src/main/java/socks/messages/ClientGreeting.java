package socks.messages;

import socks.messages.types.AuthMethod;
import socks.messages.types.SocksVersion;

public class ClientGreeting {
    SocksVersion socksVersion;
    int numberAuth;
    AuthMethod authMethod;
}
