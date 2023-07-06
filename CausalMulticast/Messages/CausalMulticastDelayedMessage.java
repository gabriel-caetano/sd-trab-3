package CausalMulticast.Messages;

import java.net.InetAddress;

/**
    A classe CausalMulticastDelayedMessage representa uma mensagem multicast causal adiada.
*/
public class CausalMulticastDelayedMessage {
    private CausalMulticastMessage message;
    private InetAddress sender;

    /**
     * Construtor da classe CausalMulticastDelayedMessage.
     * @param message CausalMulticastMessage     
     * @param sender InetAddress
     */
    public CausalMulticastDelayedMessage(CausalMulticastMessage message, InetAddress sender) {
        this.message = message;
        this.sender = sender;
    }

    /**
     * Retorna mensagem.
     * @return CausalMulticastMessage
     */
    public CausalMulticastMessage getMessage() {
        return message;
    }

    /**
     * Retorna o endereço do remetente da mensagem.
     * @return InetAddress
     */  
    public InetAddress getSender() {
        return sender;
    }

    /**
     * Retorna formatação toString.
     * @return String
     */  
    @Override
    public String toString() {
        return String.format("{message=%s, sender=%s}", message, sender);
    }
}