import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Coordinator {
    public static int port;
    public static int timeThreshold;
    public static ArrayList<Message> messages;
    public static HashMap<String, Participant> participants;

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
                        Participant p = new Participant(clientSocket.getInetAddress().toString());
                        participants.put(p.ip, p);
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
        Participant participant = participants.get(clientSocket.getInetAddress().toString());
        System.out.println("Client connected: " + clientSocket.getInetAddress());
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String command;
            while ((command = in.readLine()) != null) {
                System.out.println("Received: " + command);
                if (command.startsWith("register")) {
                    String[] tokens = command.split(" ");
                    participant.port = Integer.parseInt(tokens[1]);
                    Socket socket = new Socket(participant.ip, participant.port);
                    participant.socket = socket;
                    participant.isRegistered = true;
                } else if (command.startsWith("deregister")) {
                    participant.isRegistered = false;
                } else if (command.startsWith("disconnect")) {
                    participant.isConnected = false;
                } else if (command.startsWith("reconnect")) {
                    String[] tokens = command.split(" ");
                    participant.port = Integer.parseInt(tokens[1]);
                    participant.isConnected = true;
                    Socket socket = new Socket(participant.ip, participant.port);
                    participant.socket = socket;
                    for (Message message : messages) {
                        if (message.timestamp.getTime() > new Date().getTime() - timeThreshold) {
                            new Thread(() -> out.println("Sender: " + message.sender.id + "Message: " + message.message)).start();

                        }
                    }
                } else if (command.startsWith("msend")) {
                    String[] tokens = command.split(" ");
                    String message = tokens[1];
                    messages.add(new Message(new Date(), message, participant));
                    for (Participant p : participants.values()) {
                        if (p.isConnected && p.isRegistered) {
                            PrintWriter pout = new PrintWriter(p.socket.getOutputStream(), true);
                            new Thread(() -> pout.println("Sender: " + participant.id + "Message: " + message)).start();
                        }
                    }
                }
                else if (command.startsWith("id")) {
                    String[] tokens = command.split(" ");
                    participant.id = tokens[1];
                }

            }

        } catch (IOException e) {
            System.out.println("Error reading from client");
        }
        return;
    }
}