package socks;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.SimpleResolver;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class DnsTest {
    @Test void testDnsjavaPackage() {
        SimpleResolver simpleResolver;
    }

    @Test void testDnsResolver() throws IOException {
        DnsResolver dnsResolver = new DnsResolver(Selector.open(), DatagramChannel.open());
    }
}
