package UserApplication;

import CausalMulticast.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApplication implements ICausalMulticast {
    private CausalMulticast middleware;
    public String address = "225.0.0.1";
    public Integer port = 15050;
    
    public ClientApplication() throws IOException {
        middleware = new CausalMulticast(this.address, this.port, this);
    }

    public void start() throws IOException {
        middleware.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            String line = reader.readLine();

            if(line.equals("send")) {
                middleware.sendAllNotSentMessages();
            }
            else if(line.length() == 0 || line.charAt(0) != '>') {
                System.out.println("[WARN] Unrecognized command. Messages should start with '>'");
            }
            else if(middleware.hasDelayedMessages()) {
                System.out.println("[ERROR] Cannot send messages having delayed ones.");
            }
            else {
                middleware.mcsend(line.substring(1), this);
            }
        }
    }

    @Override
    public void deliver(String msg) {
        System.out.println("[MESSAGE_RECEIVED] " + msg);
    }
}