package socks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class DnsResolver {
    private static Logger log = LogManager.getLogger(DnsResolver.class);

    private final String PROPS_FILE_NAME = "dns.properties";

    private final Selector selector;
    private final DatagramChannel dnsChannel;
    private HashMap<String, String> ips;
    private Resolver resolver;

    private final List<DnsSubscriber> subscribers;

    public DnsResolver(Selector selector, DatagramChannel dnsChannel) throws UnknownHostException {
        readConfig();
        this.selector = selector;
        this.dnsChannel = dnsChannel;
        subscribers = new ArrayList<>();
        resolver = new SimpleResolver("9.9.9.9");
    }

    private void readConfig() {
        try {
            InputStream dnsPropsFile = DnsResolver.class.getClassLoader().getResourceAsStream(PROPS_FILE_NAME);
            Properties systemProps = new Properties(System.getProperties());
            systemProps.load(dnsPropsFile);
            System.setProperties(systemProps);

            if (log.isDebugEnabled()) {
                Properties dnsProps = new Properties();
                dnsPropsFile = DnsResolver.class.getClassLoader().getResourceAsStream(PROPS_FILE_NAME);
                dnsProps.load(dnsPropsFile);
                log.debug("Loaded dns properties: " + dnsProps);
            }
        } catch (IOException e) {
            log.error("Can not read dns config, reason: " + e.getMessage());
        }
    }

    public InetAddress resolve(String address) throws IOException {
        // create dns request
        return Address.getByName(address);
    }

    public void subscribeForResolving(DnsSubscriber subscriber) {
        subscribers.add(subscriber);
    }
}
