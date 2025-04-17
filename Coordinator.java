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
    public static volatile int port;
    public volatile static int timeThreshold;
    public volatile static ArrayList<Message> messages;
    public volatile static HashMap<String, Participant> participants;

    public static void main(String[] args) {
        participants = new HashMap<>();
        messages = new ArrayList<>();
        if (args.length != 1) {
            System.out.println("Usage: java Coordinator <config file name>");
            System.exit(1);
        }
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
                        Participant p = new Participant(clientSocket.getInetAddress().toString().substring(1));
                        if (!participants.containsKey(p.ip)) {
                            participants.put(p.ip, p);
                        }
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Error creating client connections");
            e.printStackTrace();
            System.exit(1);
        }

    }
    public static void handleClient(Socket clientSocket) {
        String configFile = "cconfig.txt";
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
        Participant participant = participants.get(clientSocket.getInetAddress().toString().substring(1));
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
                    participant.out = new PrintWriter(socket.getOutputStream(), true);
                } else if (command.startsWith("deregister")) {
                    participant.socket.close();
                    participant.socket = null;
                    participant.out = null;
                    participant.isRegistered = false;
                } else if (command.startsWith("disconnect")) {
                    participant.socket.close();
                    participant.socket = null;
                    participant.isConnected = false;
                } else if (command.startsWith("reconnect")) {
                    String[] tokens = command.split(" ");
                    participant.port = Integer.parseInt(tokens[1]);
                    participant.isConnected = true;
                    Socket socket = new Socket(participant.ip, participant.port);
                    participant.socket = socket;
                    participant.out = new PrintWriter(socket.getOutputStream(), true);
                    long currentTime = System.currentTimeMillis();
                    for (Message message : messages) {
                        System.out.println(timeThreshold);
                        if (message.timestamp.getTime() >= currentTime - (timeThreshold * 1000)) {
                            System.out.println("Sending message to " + participant.id);
                            new Thread(() -> participant.out.println("Sender: " + message.sender.id + " Message: " + message.message)).start();
                        }
                    }
                } else if (command.startsWith("msend")) {
                    if (!participant.isRegistered || !participant.isConnected) {
                        continue;
                    }
                    String[] tokens = command.split(" ");
                    String message = "";
                    for (int i = 1; i < tokens.length; i++) {
                        message += tokens[i] + " ";
                    }
                    messages.add(new Message(new Date(), message, participant));
                    String final_message = message;
                    for (Participant p : participants.values()) {
                        System.out.println(p.isConnected + " " + p.socket);
                        if (p.isConnected && (p.socket != null)) {
                            new Thread(() -> {
                                p.out.println("Sender: " + participant.id + " Message: " +final_message); 
                            }).start();

                        }
                    }
                }
                else if (command.startsWith("id")) {
                    String[] tokens = command.split(" ");
                    participant.id = tokens[1];
                }

            }

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            System.out.println("Error reading from client");
        }
        return;
    }
}