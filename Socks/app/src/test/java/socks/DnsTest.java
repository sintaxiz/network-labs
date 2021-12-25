package socks;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

public class DnsTest {
    @Test void testDnsjavaPackage() throws IOException, InterruptedException {
        Resolver simpleResolver = new SimpleResolver("9.9.9.9");
        Message weatherRequest = Message.newQuery(Record.newRecord(Name.fromString("weather.nsu.ru."), Type.A, DClass.IN));
        simpleResolver.sendAsync(weatherRequest, new ResolverListener() {
            @Override
            public void receiveMessage(Object id, Message m) {
                System.out.println(m);
            }

            @Override
            public void handleException(Object id, Exception e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(100000000);
    }

    @Test void testDnsResolver() throws IOException {
        DnsResolver dnsResolver = new DnsResolver(Selector.open(), DatagramChannel.open());
    }
}
