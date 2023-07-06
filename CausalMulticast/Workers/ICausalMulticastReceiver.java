package CausalMulticast.Workers;

import java.net.InetAddress;
import java.net.SocketException;

import CausalMulticast.CausalMulticastMessage;

/**
    A interface ICausalMulticastReceiver define um padrão para um receptor do multicast causal.
*/
public interface ICausalMulticastReceiver {

    /**
     * Processa uma mensagem recebida.
     * @param message ICausalMulticastReceiver
     * @param sender InetAddress
     */
    void messageReceived(CausalMulticastMessage message, InetAddress sender);
}