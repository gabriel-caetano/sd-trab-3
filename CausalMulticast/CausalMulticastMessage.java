package CausalMulticast;

import java.io.*;
import java.util.Arrays;

public class CausalMulticastMessage implements Serializable {
    private String content;
    private int[] clockArray;

    public CausalMulticastMessage(String content, int[] clockArray) {
        this.content = content;
        this.clockArray = clockArray;
    }

    public String getContent() {
        return content;
    }

    public int[] getClockArray() {
        return clockArray;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);

        os.writeObject(this);

        return out.toByteArray();
    }

    public static CausalMulticastMessage deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);

        return (CausalMulticastMessage) is.readObject();
    }

    @Override
    public String toString() {
        return String.format("{content='%s', clockArray=%s}", content, Arrays.toString(clockArray));
    }
}
