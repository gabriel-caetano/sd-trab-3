package CausalMulticast.Workers;

import CausalMulticast.CausalMulticastMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


/**
    A classe CausalMulticastReceiver é responsável por receber mensagens em um socket UDP e repassá-las
    para o receptor do multicast causal.
*/
public class CausalMulticastReceiver extends Thread {
    private ICausalMulticastReceiver receiver;
    private DatagramSocket socket;
    private final byte[] buffer = new byte[65535];

    /**
     * Construtor da classe CausalMulticastReceiver.
     * @param receiver ICausalMulticastReceiver
     * @param port Integer
     * @throws SocketException
     */
    public CausalMulticastReceiver(ICausalMulticastReceiver receiver, Integer port) throws SocketException {
        this.receiver = receiver;
        this.socket = new DatagramSocket(port + 1);
    }

    /**
     * Sobrescreve o método run da classe Thread.
     * O método executa a lógica de recepção contínua de pacotes e repassa as mensagens recebidas
     * para o receptor do multicast causal.
     */
    @Override
    public void run() {
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                receiver.messageReceived(CausalMulticastMessage.deserialize(packet.getData()), packet.getAddress());
            }
        }
        catch (Exception e) {
            e.printStackTrace();

            System.out.println("[RECEIVER][ERROR] Erro ao iniciar serviço receiver");
        }
    }
}
