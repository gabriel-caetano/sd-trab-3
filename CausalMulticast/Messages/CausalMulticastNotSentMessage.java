package CausalMulticast.Messages;

import java.net.InetAddress;

/**
    A classe CausalMulticastNotSentMessage representa uma mensagem multicast causal que não foi enviada anteriormente.
*/
public class CausalMulticastNotSentMessage {
    private byte[] serializedMessage;
    private InetAddress destination;

    /**
     * Construtor da classe CausalMulticastNotSentMessage.
     * @param serializedMessage byte[]     
     * @param destination InetAddress
     */
    public CausalMulticastNotSentMessage(byte[] serializedMessage, InetAddress destination) {
        this.serializedMessage = serializedMessage;
        this.destination = destination;
    }

    /**
     * Retorna mensagem serializada.
     * @return byte[]
     */
    public byte[] getSerializedMessage() {
        return serializedMessage;
    }

    /**
     * Retorna endereço destino.
     * @return destination
     */  
    public InetAddress getDestination() {
        return destination;
    }
}