import java.io.*;
import java.net.*;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;
    private String role;

    public Client(String ip, int port, String role) throws IOException {
        this.clientSocket = new Socket(ip, port);
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.stdIn = new BufferedReader(new InputStreamReader(System.in));
        this.role = role.toUpperCase();
    }

    public void start() {
        try {
            // Send the role to the server
            out.println(role);

            // Read server responses and print them
            Thread serverListener = new Thread(() -> {
                String serverResponse;
                try {
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println("Server: " + serverResponse);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverListener.start();

            // Read user input and send to server
            String userInput;
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
        if (args.length != 3) {
            System.out.println("Usage: java Client <server_ip> <port> <role>");
            return;
        }

        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String role = args[2];

        try {
            Client client = new Client(ip, port, role);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
