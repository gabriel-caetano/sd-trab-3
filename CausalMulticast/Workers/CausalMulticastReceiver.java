package CausalMulticast.Workers;

import CausalMulticast.CausalMulticastMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CausalMulticastReceiver extends Thread {
    private ICausalMulticastReceiver client;
    private DatagramSocket socket;
    private final byte[] buffer = new byte[65535];

    public CausalMulticastReceiver(ICausalMulticastReceiver client, Integer port) throws SocketException {
        this.client = client;
        this.socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                client.messageReceived(CausalMulticastMessage.deserialize(packet.getData()), packet.getAddress());
            }
        }
        catch (Exception e) {
            e.printStackTrace();

            System.out.println("[ERROR] Received exception on receiver thread");
        }
    }
}
