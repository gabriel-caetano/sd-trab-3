package CausalMulticast;

import java.net.InetAddress;

public class CausalMulticastDelayedMessage {
    private CausalMulticastMessage message;
    private InetAddress sender;

    public CausalMulticastDelayedMessage(CausalMulticastMessage message, InetAddress sender) {
        this.message = message;
        this.sender = sender;
    }

    public CausalMulticastMessage getMessage() {
        return message;
    }

    public InetAddress getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return String.format("{message=%s, sender=%s}", message, sender);
    }
}