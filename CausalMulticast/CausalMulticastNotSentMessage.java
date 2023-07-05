package CausalMulticast;

import java.net.InetAddress;

public class CausalMulticastNotSentMessage {
    private byte[] serializedMessage;
    private InetAddress destination;

    public CausalMulticastNotSentMessage(byte[] serializedMessage, InetAddress destination) {
        this.serializedMessage = serializedMessage;
        this.destination = destination;
    }

    public byte[] getSerializedMessage() {
        return serializedMessage;
    }

    public InetAddress getDestination() {
        return destination;
    }
}