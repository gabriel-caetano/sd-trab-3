package CausalMulticast.Interfaces;

/**
    A interface ICausalMulticast define um padrão para um cliente do multicast causal.
*/
public interface ICausalMulticast {

    /**
     * Entrega uma mensagem.
     * @param msg String
     */
    void deliver(String msg);
}
