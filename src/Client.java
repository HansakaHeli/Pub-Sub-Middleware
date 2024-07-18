import java.io.*;
import java.net.*;

public class Client {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;

    public Client(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        stdIn = new BufferedReader(new InputStreamReader(System.in));
    }

    public void start() {
        String userInput;
        try {
            System.out.println("Connected to server. Type your messages:");
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                if (userInput.equalsIgnoreCase("terminate")) {
                    System.out.println("Connection terminated by client.");
                    break;
                }
            }
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() throws IOException {
        out.close();
        in.close();
        stdIn.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Client <server_ip> <port>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            Client client = new Client(ip, port);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
