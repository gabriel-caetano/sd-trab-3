package CausalMulticast.Workers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

/**
    A classe CausalMulticastDiscovery é responsável por descobrir e manter uma lista de endereços IP de clientes
    que desejam participar de um serviço de multicast causal.
    Ao ser executada em uma thread, a classe envia uma mensagem de descoberta para um grupo multicast e recebe
    mensagens de resposta dos clientes que desejam participar do serviço.
*/

public class CausalMulticastDiscovery extends Thread {
    private static final String message = "PARTICIPAR";
    private ArrayList<InetAddress> lstDiscoveredIpAddresses;
    private MulticastSocket multicastSocket;
    private InetAddress group;
    private Integer port;

    /**
     * Construtor da classe CausalMulticastDiscovery
     * @param ip String
     * @param port Integer
     * @throws IOException
     */
    public CausalMulticastDiscovery(String ip, Integer port) throws IOException {
        this.lstDiscoveredIpAddresses = new ArrayList<>();
        this.multicastSocket = new MulticastSocket(port);
        this.group = InetAddress.getByName(ip);
        this.port = port;
    }

    /**
     * Retorna a lista de endereços IP dos clientes descobertos
     * @return lstDiscoveredIpAddresses
     */
    public List<InetAddress> getDiscoveredIpAddresses() {
        return lstDiscoveredIpAddresses;
    }

    /**
     * Sobrescreve o método run da classe Thread.
     * O método executa a lógica de descoberta e atualização da lista de endereços IP dos clientes.
     */
    @Override
    public void run() {
        byte[] buf = new byte[56];

        try {
            System.out.println("[DISCOVERY] Serviço iniciado...");
            multicastSocket.joinGroup(group);

            sendMessage();

            while(true) {
                DatagramPacket receivedMessage = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(receivedMessage);
                String messageContent = new String(receivedMessage.getData(), 0, receivedMessage.getLength(), "UTF-8");
                
                if(messageContent.equals(message)) {
                    synchronized (this) {
                        InetAddress clientAddress = receivedMessage.getAddress();
                        Boolean added = false;

                        for(InetAddress address : lstDiscoveredIpAddresses) {
                            if(address.equals(clientAddress)) {
                                added = true;
                            }
                        }

                        if(!added) {
                            System.out.println(String.format("[DISCOVERY] Cliente %s conectado!", clientAddress.getHostAddress()));

                            lstDiscoveredIpAddresses.add(clientAddress);
                            lstDiscoveredIpAddresses.sort((a1, a2) -> a1.getHostAddress().compareTo(a2.getHostAddress()));

                            sendMessage();
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();

            System.out.println("[DISCOVERY][ERROR] Erro ao iniciar serviço discovery");
        }
    }

    /**
     * Envia uma mensagem para o grupo multicast.
     * @throws IOException
     */
    private void sendMessage() throws IOException {
        multicastSocket.send(new DatagramPacket(message.getBytes("UTF-8"), message.length(), group, this.port));
    }
}
