import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Main {
    // TODO: Make variables for all the parameters in the config file
    String coordinatorIP;
    int coordinatorPort;

    public static void main(String[] args) {
        String coordinatorIP = args[0];
        int coordinatorPort = Integer.parseInt(args[1]);
        // TODO: Read from config file to find necessary info instead of args ^
    }

    public void handleCommand() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            sendToCoordinator(command);
        }
    }

    private void sendToCoordinator(String command) {
        try {
            Socket socket = new Socket(coordinatorIP, coordinatorPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
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