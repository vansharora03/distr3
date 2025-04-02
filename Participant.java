import java.io.PrintWriter;
import java.net.Socket;

public class Participant {
    public String ip;
    public int port;
    public boolean isRegistered;
    public boolean isConnected;
    public Socket socket;
    public PrintWriter out;
    public String id;

    public Participant(String ip) {
        this.ip = ip;
        this.port = -1;
        this.isRegistered = false;
        this.isConnected = true;
        this.socket = null;
        this.out = null;
    }
}