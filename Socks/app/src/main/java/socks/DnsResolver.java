package socks;

import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DnsResolver {
    private final Selector selector;
    private final DatagramChannel dnsChannel;
    private HashMap<String, String> ips;

    private final List<DnsSubscriber> subscribers;

    public DnsResolver(Selector selector, DatagramChannel dnsChannel) {
        this.selector = selector;
        this.dnsChannel = dnsChannel;
        subscribers = new ArrayList<>();
    }

    public void resolve(String address) throws ClosedChannelException {
        // create dns request
        dnsChannel.register(selector, SelectionKey.OP_WRITE, this);
    }

    public void subscribeForResolving(DnsSubscriber subscriber) {
        subscribers.add(subscriber);
    }
}
