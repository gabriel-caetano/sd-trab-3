import UserApplication.*;

public class Main {
    public static void main(String[] args) {
        try {
            new ClientApplication().start();
        }
        catch (Exception e) {
            e.printStackTrace();

            System.out.println("Failed to start client application");
        }
    }
}
