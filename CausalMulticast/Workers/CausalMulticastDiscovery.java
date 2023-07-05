package CausalMulticast.Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class CausalMulticastDiscovery extends Thread {
    private static final String helloMessage = "JOINING_GROUP";
    private ArrayList<InetAddress> discoveredIpAddresses;
    private MulticastSocket multicastSocket;
    private InetAddress group;
    private Integer port;


    public CausalMulticastDiscovery(String ip, Integer port) throws IOException {
        this.port = port;
        multicastSocket = new MulticastSocket(port);
        group = InetAddress.getByName(ip);

        discoveredIpAddresses = new ArrayList<>();
    }

    public List<InetAddress> getDiscoveredIpAddresses() {
        return discoveredIpAddresses;
    }

    @Override
    public void run() {
        byte[] buf = new byte[56];

        try {
            System.out.println("[DISCOVERY] Service started");
            multicastSocket.joinGroup(group);

            sendHelloMessage();

            while(true) {
                DatagramPacket receivedMessage = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(receivedMessage);
                String messageContent = new String(receivedMessage.getData(), 0, receivedMessage.getLength(), "UTF-8");

                if(messageContent.equals(helloMessage)) {
                    synchronized (this) {
                        InetAddress clientAddress = receivedMessage.getAddress();
                        Boolean added = false;

                        for(InetAddress address : discoveredIpAddresses) {
                            if(address.equals(clientAddress)) {
                                added = true;
                            }
                        }

                        if(!added) {
                            System.out.println(String.format("[DISCOVERY] Client %s connected", clientAddress.getHostAddress()));

                            discoveredIpAddresses.add(clientAddress);
                            discoveredIpAddresses.sort((a1, a2) -> a1.getHostAddress().compareTo(a2.getHostAddress()));

                            sendHelloMessage();
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();

            System.out.println("[DISCOVERY][ERROR] Received exception on discovery thread");
        }
    }

    private void sendHelloMessage() throws IOException {
        DatagramPacket helloDatagramPacket = new DatagramPacket(helloMessage.getBytes("UTF-8"), helloMessage.length(), group, this.port);

        multicastSocket.send(helloDatagramPacket);
    }
}
