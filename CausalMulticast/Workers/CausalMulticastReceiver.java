package CausalMulticast.Workers;

import CausalMulticast.CausalMulticastMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CausalMulticastReceiver extends Thread {
    private ICausalMulticastReceiver receiver;
    private DatagramSocket socket;
    private final byte[] buffer = new byte[65535];

    public CausalMulticastReceiver(ICausalMulticastReceiver receiver, Integer port) throws SocketException {
        this.receiver = receiver;
        this.socket = new DatagramSocket(port + 1);
    }

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

            System.out.println("[ERROR] Received exception on receiver thread");
        }
    }
}
