import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 1025);
        } catch (Exception e) {
            System.out.println("Error connecting to coordinator");
            System.exit(1);
        }
    }
}   