package CausalMulticast.Messages;

import java.io.*;
import java.util.Arrays;

/**
    A classe CausalMulticastMessage representa uma mensagem multicast causal.
*/
public class CausalMulticastMessage implements Serializable {
    private String content;
    private int[] clockArray;

    /**
     * Construtor da classe CausalMulticastMessage.
     * @param content String     
     * @param clockArray int[]
     */
    public CausalMulticastMessage(String content, int[] clockArray) {
        this.content = content;
        this.clockArray = clockArray;
    }

    /**
     * Retorna mensagem.
     * @return CausalMulticastMessage
     */
    public String getContent() {
        return content;
    }

    /**
     * Retorna relógio lógico.
     * @return int[]
     */
    public int[] getClockArray() {
        return clockArray;
    }

    /**
     * Serializa a mensagem.
     * @return byte[]
     * @throws IOException
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);

        os.writeObject(this);

        return out.toByteArray();
    }

    /**
     * Deserializa a mensagem.
     * @param data byte[]
     * @return CausalMulticastMessage
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static CausalMulticastMessage deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);

        return (CausalMulticastMessage) is.readObject();
    }

    /**
     * Retorna formatação toString.
     * @return String
     */  
    @Override
    public String toString() {
        return String.format("{content='%s', clockArray=%s}", content, Arrays.toString(clockArray));
    }
}
