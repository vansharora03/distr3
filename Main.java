import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Main {
    String coordinatorIP;
    int coordinatorPort;
    String id;
    String messageFile;
    int myPort;
    public volatile ServerSocket serverSocket;
    public volatile Socket socket;
    public volatile Thread listeningThread;

    public Main(String configFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        this.id = reader.readLine().trim();
        this.messageFile = reader.readLine().trim();
        String[] line = reader.readLine().trim().split(" ");
        this.coordinatorIP = line[0];
        this.coordinatorPort = Integer.parseInt(line[1]);
        Socket socket = new Socket(coordinatorIP, coordinatorPort);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("id " + id);
        socket.close();
        this.serverSocket = null;
        this.listeningThread = null;
    }

    public void start() {
        try {
            Runnable handleCommands = () -> {
                try {
                    handleCommands();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            new Thread(handleCommands).start();
            System.out.println("Created thread A for user commands");

            Runnable listenMessage = () -> {
                try {
                    listenMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            listeningThread = new Thread(listenMessage);
            listeningThread.start();
            System.out.println("Created thread B for message reception");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <config file>");
            System.exit(1);
        }
        try {
            Main main = new Main(args[0]);
            System.out.println("Participant started. Connecting to Coordinator at " + main.coordinatorIP + " "
                    + main.coordinatorPort);
            main.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCommands() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine();

            if (command.startsWith("deregister") || command.startsWith("disconnect")) {
                socket.close();
                serverSocket.close();
                listeningThread = new Thread(() -> {
                    listenMessage();
                });
                listeningThread.start();
            } 

            // Handle the register command with user defined port
            if (command.startsWith("register ")) {
                try {
                    myPort = Integer.parseInt(command.split(" ")[1]);

                    serverSocket = new ServerSocket(myPort);
                    sendToCoordinator("register " + myPort);

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            } else if (command.startsWith("reconnect")) {
                try {

                    myPort = Integer.parseInt(command.split(" ")[1]);
                    serverSocket = new ServerSocket(myPort);
                    sendToCoordinator("reconnect " + myPort);
                    System.out.println("Reconnected to coordinator at port " + myPort);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                sendToCoordinator(command);
            }

        }
    }

    public void listenMessage() {
        socket = null;
        serverSocket = null;
        while (true) {
                if (serverSocket == null)
                    continue;
                if (socket == null) {
                    try {
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = in.readLine();

                    if (message != null) {
                        logMessages(message);
                    }
                } catch (IOException e) {
                    Thread.currentThread().interrupt();
                }
        }
    }

    private void sendToCoordinator(String command) throws IOException {
        
        try (Socket socket = new Socket(coordinatorIP, coordinatorPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(command);
            System.out.println("Sent command: " + command);
        } catch (IOException e) {
            System.out.println("Error sending command to coordinator.");
        }
    }

    public void logMessages(String message) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(messageFile, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to log file.");
        }
    }
}
