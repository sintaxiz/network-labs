package socks;

import org.junit.jupiter.api.Test;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.Arrays;

public class DnsTest {

    public static final int DNS_PORT = 53;
    public static final int MILLIS_FOR_SLEEP = 100000000;
    public static final String DNS_SERVER_IP = "9.9.9.9";
    public static final int BUFF_ANSW_SIZE = 1024;

    @Test void testDnsjavaPackage() throws IOException, InterruptedException {
        DatagramSocket socket = new DatagramSocket();
        Record queryRecord = Record.newRecord(Name.fromString("weather.nsu.ru."), Type.A, DClass.IN);
        Message queryMessage = Message.newQuery(queryRecord);
        byte[] queryBytes = queryMessage.toWire();
        DatagramPacket dnsRequest = new DatagramPacket(queryBytes,
                queryBytes.length,
                InetAddress.getByName(DNS_SERVER_IP),
                DNS_PORT);
        socket.send(dnsRequest);

        System.out.println("successfully send dns request");
        byte[] answerBuff = new byte[BUFF_ANSW_SIZE];
        DatagramPacket answerPacket = new DatagramPacket(answerBuff, BUFF_ANSW_SIZE);
        socket.receive(answerPacket);
//        byte[] recordBytes = Arrays.copyOf(answerPacket.getData(), answerPacket.getLength());
        byte[] recordBytes = answerPacket.getData();
        Record record = Record.fromWire(recordBytes, Section.ANSWER);
        System.out.println("receive: " + record);
    }

    @Test void testDnsResolver() throws IOException {
        DnsResolver dnsResolver = new DnsResolver(Selector.open(), DatagramChannel.open());
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
    }
}
