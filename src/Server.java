import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private ServerSocket serverSocket;
    private Map<String, List<ClientHandler>> topicSubscribers = new HashMap<>();

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        System.out.println("Server started, waiting for clients...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandler.start();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void addSubscriber(String topic, ClientHandler client) {
        topicSubscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(client);
    }

    public synchronized void removeSubscriber(String topic, ClientHandler client) {
        List<ClientHandler> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            subscribers.remove(client);
            if (subscribers.isEmpty()) {
                topicSubscribers.remove(topic);
            }
        }
    }

    public synchronized void broadcast(String topic, String message, ClientHandler sender) {
        List<ClientHandler> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            for (ClientHandler client : subscribers) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
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
    private String topic;

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

            // Read role and topic
            role = in.readLine().toUpperCase();
            topic = in.readLine().toUpperCase();

            if (!role.equals("PUBLISHER") && !role.equals("SUBSCRIBER")) {
                out.println("Invalid role. Disconnecting.");
                closeConnection();
                return;
            }

            if (role.equals("SUBSCRIBER")) {
                server.addSubscriber(topic, this);
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equalsIgnoreCase("terminate")) {
                    System.out.println("Client terminated the connection.");
                    break;
                }
                if (role.equals("PUBLISHER")) {
                    System.out.println("Received from Publisher: " + inputLine);
                    server.broadcast(topic, inputLine, this);
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
            if (role.equals("SUBSCRIBER")) {
                server.removeSubscriber(topic, this);
            }
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
