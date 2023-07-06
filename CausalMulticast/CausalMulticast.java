package CausalMulticast;

import CausalMulticast.Handlers.CausalMulticastDiscovery;
import CausalMulticast.Handlers.CausalMulticastReceiver;
import CausalMulticast.Interfaces.ICausalMulticast;
import CausalMulticast.Interfaces.ICausalMulticastReceiver;
import CausalMulticast.Messages.CausalMulticastDelayedMessage;
import CausalMulticast.Messages.CausalMulticastMessage;
import CausalMulticast.Messages.CausalMulticastNotSentMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
    A classe CausalMulticast é responsável por implementar a lógica do middleware de multicast causal.
    O middleware gerencia a comunicação entre os clientes, permitindo o envio e recebimento de mensagens
    multicast causal. Ele também lida com a descoberta de clientes e o controle do relógio lógico para
    garantir a ordem causal das mensagens.
*/
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

    /**
     * Construtor da classe CausalMulticast.
     * @param ip String     
     * @param port Integer
     * @param client ICausalMulticast
     * @throws IOException
     */
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

    /**
     * Inicia middleware e seus respectivos componentes: o desbravador e o receptor.
     */
    public void start() {
        System.out.println(String.format("[MIDDLEWARE] Host %s iniciado.", currentAddress.getHostAddress()));
        discovery.start();
        receiver.start();
    }

    /**
     * Envia uma mensagem para os clientes.
     * @param msg String
     * @param client ICausalMulticast
     */
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

    /**
     * Envia todas as mensagens que não foram enviadas anteriormente devido a "atrasos".
     * @throws IOException
     */
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

    /**
     * Implementa o método messageReceived da classe ICausalMulticastReceiver.
     * @param message CausalMulticastMessage
     * @param sender InetAddress
     */
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

    /**
     * Verifica se há mensagens adiadas.
     * @return Boolean
     */
    public boolean hasDelayedMessages() {
        return delayedMessages.size() > 0;
    }

    /**
     * Verifica as mensagens adiadas e as entrega, se possível.
     */
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

    /**
     * Verifica se uma mensagem pode ser entregue com base no relógio lógico atual.
     * @param message CausalMulticastMessage
     * @return Boolean
     */
    private boolean messageCanBeDelivered(CausalMulticastMessage message) {
        for(int i = 0; i < clockArray.length; i++) {
            if(message.getClockArray()[i] > clockArray[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Entrega uma mensagem recebida pelo middleware de multicast causal.
     * Atualiza o relógio lógico e chama o cliente para lidar com a entrega da mensagem.
     * @param message CausalMulticastMessage
     * @param sender InetAddress
     */
    private void deliverMessage(CausalMulticastMessage message, InetAddress sender) {
        int indexOfSource = getIndexForAddress(sender);

        if(indexOfSource != indexOfInstanceInClockArray) {
            clockArray[indexOfSource]++;
        }

        client.deliver(message.getContent());
    }

    /**
     * Inicializa o array de relógio lógico com base nos endereços IP descobertos.
     */
    private void initializeClockArray() {
        System.out.println("[MIDDLEWARE] Inicializando array de clocks");

        List<InetAddress> allIps = discovery.getDiscoveredIpAddresses();
        this.clockArray = new int[allIps.size()];
        this.indexOfInstanceInClockArray = getIndexForAddress(currentAddress);

        for(int i = 0; i < allIps.size(); i++) {
            clockArray[i] = 0;
        }
    }

    /**
     * Obtém o índice correspondente a um endereço IP no array de relógio lógico.
     * @param address InetAddress
     * @return int
     */
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