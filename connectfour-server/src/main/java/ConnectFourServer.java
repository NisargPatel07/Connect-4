import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

// this is server class
public class ConnectFourServer {
    private static final int PORT = 12345; // server port
    private static final List<ClientHandler> waitingClients = new ArrayList<>(); // list of waiting clients
    private static final ExecutorService pool = Executors.newCachedThreadPool(); // thread pool

    // start the server
    public static void startServer() {
        try (ServerSocket listener = new ServerSocket(PORT)) {
            ServerGUI.log("Server is running on port " + PORT);
            while (true) {
                Socket clientSocket = listener.accept(); // accept client
                ServerGUI.log("Client connected: " + clientSocket);
                ClientHandler handler = new ClientHandler(clientSocket); // create handler
                pool.execute(handler); // run handler
            }
        } catch (IOException e) {
            ServerGUI.log("Server error: " + e.getMessage());
        }
    }

    // add client to waiting list and match if 2 players are there
    public static synchronized void addWaitingClient(ClientHandler client) {
        waitingClients.add(client);
        if (waitingClients.size() >= 2) {
            ClientHandler p1 = waitingClients.remove(0);
            ClientHandler p2 = waitingClients.remove(0);
            GameSession session = new GameSession(p1, p2); // create game session
            p1.setGameSession(session);
            p2.setGameSession(session);
            pool.execute(session); // run game session
        }
    }
}