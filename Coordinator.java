import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Coordinator {
    public int port;
    public int timeThreshold;
    public Message[] messages;
    public HashMap<String, Main> participants;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Coordinator <config file name>");
            System.exit(1);
        }
        String configFile = args[0];
        int port = -1;
        int timeThreshold = -1;

        // Parse config file
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            String line = br.readLine();
            port = Integer.parseInt(line);
            line = br.readLine();
            timeThreshold = Integer.parseInt(line);
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading config file");
            System.exit(1);
        }

        System.out.println("Listening on port: " + port);
        System.out.println(" with time threshold: " + timeThreshold);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Error creating client connections");
            System.exit(1);
        }

    }

    public static void handleClient(Socket clientSocket) {
        System.out.println("Client connected: " + clientSocket.getInetAddress());
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String command;
            while ((command = in.readLine()) != null) {
                System.out.println("Received command: " + command);
            }
        } catch (IOException e) {
            System.out.println("Error reading from client");
        }
        return;
    }
}