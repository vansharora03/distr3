import java.net.Socket;

public class Participant {
    public String ip;
    public int port;
    public boolean isRegistered;
    public boolean isConnected;
    public Socket socket;

    public Participant(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.isRegistered = false;
        this.isConnected = true;
        this.socket = null;
    }
}