package CausalMulticast;

import CausalMulticast.Workers.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class CausalMulticast implements ICausalMulticastReceiver {

    private String ip;
    private Integer port;
    private int[] clockArray;
    private int indexOfInstanceInClockArray;
    private InetAddress currentAddress;
    private DatagramSocket datagramSocket;
    private ICausalMulticast client;
    private CausalMulticastDiscovery discovery;
    private CausalMulticastReceiver receiver;
    private ArrayList<CausalMulticastDelayedMessage> delayedMessages;
    private ArrayList<CausalMulticastNotSentMessage> notSentMessages;


    public CausalMulticast(String ip, Integer port, ICausalMulticast client) throws IOException {
        this.ip = ip;
        this.port = port;
        this.indexOfInstanceInClockArray = -1;
        this.currentAddress = InetAddress.getLocalHost();
        this.datagramSocket = new DatagramSocket();
        this.client = client;
        this.discovery = new CausalMulticastDiscovery(this.ip, this.port);
        this.receiver = new CausalMulticastReceiver(this, this.port);
        this.delayedMessages = new ArrayList<CausalMulticastDelayedMessage>();
        this.notSentMessages = new ArrayList<CausalMulticastNotSentMessage>();
    }

    public void start() {
        System.out.println(String.format("[MIDDLEWARE] Host %s iniciado.", currentAddress.getHostAddress()));
        discovery.start();
        receiver.start();
    }

    public void mcsend(String msg, ICausalMulticast client) {
        if(indexOfInstanceInClockArray < 0) {
            initializeClockArray();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            CausalMulticastMessage message = new CausalMulticastMessage(msg, clockArray);
            byte[] messageBytes = message.serialize();
            ArrayList<InetAddress> addressToSend = new ArrayList<InetAddress>();

            for(InetAddress address : discovery.getDiscoveredIpAddresses()) {
                System.out.print(String.format("Send to %s (Y/n)? ", address));

                if(reader.readLine().equalsIgnoreCase("y")) {
                    addressToSend.add(address);
                }
                else {
                    notSentMessages.add(new CausalMulticastNotSentMessage(messageBytes, address));
                }
            }

            for(InetAddress address : addressToSend) {
                DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address, this.port + 1);
                datagramSocket.send(packet);
            }

            clockArray[indexOfInstanceInClockArray]++;
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Failed to send message");
        }
    }

    public void sendAllNotSentMessages() throws IOException {
        synchronized (this) {
            while(notSentMessages.size() > 0) {
                CausalMulticastNotSentMessage message = notSentMessages.get(0);
                byte[] messageBytes = message.getSerializedMessage();
                DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, message.getDestination(), this.port + 1);

                System.out.println(String.format("[MIDDLEWARE] Sent delayed message to %s", message.getDestination().getHostAddress()));

                datagramSocket.send(packet);
                notSentMessages.remove(0);
            }
        }
    }

    @Override
    public void messageReceived(CausalMulticastMessage message, InetAddress sender) {
        synchronized (this) {
            if(indexOfInstanceInClockArray < 0) {
                initializeClockArray();
            }

            System.out.println("[DEBUG] Middleware recebeu mensagem: "+message.toString()+"\t Clocl local atual: "+Arrays.toString(clockArray));

            if(!messageCanBeDelivered(message)) {
                delayedMessages.add(new CausalMulticastDelayedMessage(message, sender));
                System.out.printf("[%s][DELAYED] Mensagem %s foi adiada. Mensagens adiadas: %s \n", sender.getHostAddress(), message.getContent(), Arrays.toString(delayedMessages.toArray()));
                return;
            }

            deliverMessage(message, sender);

            checkDelayedMessages();
        }
    }

    public boolean hasDelayedMessages() {
        return delayedMessages.size() > 0;
    }

    private void checkDelayedMessages() {
        for(int i = 0; i < delayedMessages.size(); i++) {
            CausalMulticastDelayedMessage delayedMessage = delayedMessages.get(i);

            if(messageCanBeDelivered(delayedMessage.getMessage())) {
                deliverMessage(delayedMessage.getMessage(), delayedMessage.getSender());
                delayedMessages.remove(i);

                checkDelayedMessages();
                break;
            }
        }
    }

    private boolean messageCanBeDelivered(CausalMulticastMessage message) {
        for(int i = 0; i < clockArray.length; i++) {
            if(message.getClockArray()[i] > clockArray[i]) {
                return false;
            }
        }

        return true;
    }

    private void deliverMessage(CausalMulticastMessage message, InetAddress sender) {
        int indexOfSource = getIndexForAddress(sender);

        if(indexOfSource != indexOfInstanceInClockArray) {
            clockArray[indexOfSource]++;
        }

        client.deliver(message.getContent());
    }

    private void initializeClockArray() {
        System.out.println("[MIDDLEWARE] Inicializando array de clocks");

        List<InetAddress> allIps = discovery.getDiscoveredIpAddresses();
        this.clockArray = new int[allIps.size()];
        this.indexOfInstanceInClockArray = getIndexForAddress(currentAddress);

        for(int i = 0; i < allIps.size(); i++) {
            clockArray[i] = 0;
        }
    }

    private int getIndexForAddress(InetAddress address) {
        List<InetAddress> allIps = discovery.getDiscoveredIpAddresses();

        for(int i = 0; i < allIps.size(); i++) {
            if(allIps.get(i).getHostAddress().equals(address.getHostAddress())) {
                return i;
            }
        }

        return -1;
    }
}