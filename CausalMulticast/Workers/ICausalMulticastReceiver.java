package CausalMulticast.Workers;

import java.net.InetAddress;

import CausalMulticast.CausalMulticastMessage;

public interface ICausalMulticastReceiver {
    void messageReceived(CausalMulticastMessage message, InetAddress sender);
}