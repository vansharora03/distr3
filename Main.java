import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Main {
    // TODO: Make variables for all the parameters in the config file
    String coordinatorIP;
    int coordinatorPort;
    int id;
    String messageFile;

    public Main(String configFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        this.id = Integer.parseInt(reader.readLine().trim());
        this.messageFile = reader.readLine().trim();
        String[] line = reader.readLine().trim().split(" ");
        this.coordinatorIP = line[0];
        this.coordinatorPort = Integer.parseInt(line[1]);
    }

    public void start() {
        new Thread().start();
        System.out.println("Started");
        handleCommands();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java main <config file name>");
            System.exit(1);
        }
        try {
            Main main = new Main(args[0]);
            Socket socket = new Socket(main.coordinatorIP, main.coordinatorPort);
            System.out.println("Server started on " + main.coordinatorIP + " at port " + main.coordinatorPort);
            main.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleCommands() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine();
            sendToCoordinator(command);
        }
    }

    private void sendToCoordinator(String command) {
        return;
    }

    // TODO: Log messages to a file
    public void logMessages(String message) {
        return;
    }

    // TODO: Listen for message
    public void listenMessage() {
        return;
    }
}