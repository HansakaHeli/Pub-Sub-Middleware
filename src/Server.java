import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        System.out.println("Server started, waiting for clients...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                clientHandler.start();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender && client.getRole().equals("SUBSCRIBER")) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try {
            Server server = new Server(port);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String role;

    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
    }

    public String getRole() {
        return role;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine = in.readLine();
            role = inputLine.toUpperCase();

            if (!role.equals("PUBLISHER") && !role.equals("SUBSCRIBER")) {
                out.println("Invalid role. Disconnecting.");
                closeConnection();
                return;
            }

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equalsIgnoreCase("terminate")) {
                    System.out.println("Client terminated the connection.");
                    break;
                }
                if (role.equals("PUBLISHER")) {
                    System.out.println("Received from Publisher: " + inputLine);
                    server.broadcast(inputLine, this);
                } else {
                    System.out.println("Received from Subscriber: " + inputLine);
                }
            }
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            server.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
