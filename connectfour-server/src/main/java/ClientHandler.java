import java.io.*;
import java.net.Socket;
import java.util.Queue;
import java.util.Set;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Set<String> usernames;
    private final Queue<ClientHandler> waitingClients;

    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private ClientHandler opponent;
    private boolean inGame = false;

    public ClientHandler(Socket socket, Set<String> usernames, Queue<ClientHandler> waitingClients) {
        this.socket = socket;
        this.usernames = usernames;
        this.waitingClients = waitingClients;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Login process
            out.println("ENTER_USERNAME");
            while (true) {
                String name = in.readLine();
                if (name == null) return;
                synchronized (usernames) {
                    if (!usernames.contains(name)) {
                        usernames.add(name);
                        username = name;
                        out.println("USERNAME_ACCEPTED");
                        System.out.println("✅ Username accepted: " + name);
                        break;
                    } else {
                        out.println("USERNAME_TAKEN");
                    }
                }
            }

            // Wait or pair up
            synchronized (waitingClients) {
                if (!waitingClients.isEmpty()) {
                    opponent = waitingClients.poll();
                    opponent.setOpponent(this);
                    new GameSession(this, opponent).start();
                } else {
                    waitingClients.add(this);
                    out.println("WAITING_FOR_OPPONENT");
                }
            }

            // Keep connection alive
            while (true) {
                String input = in.readLine();
                if (input == null || input.equalsIgnoreCase("QUIT")) break;
                if (inGame && opponent != null) {
                    opponent.send(input);
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Client disconnected: " + username);
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            if (username != null) {
                usernames.remove(username);
            }
            synchronized (waitingClients) {
                waitingClients.remove(this);
            }
        }
    }

    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public void send(String msg) {
        out.println(msg);
    }

    public String getUsername() {
        return username;
    }
}