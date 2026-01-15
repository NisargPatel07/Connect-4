public class GameSession implements Runnable {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final int[][] board = new int[6][7]; // 6 rows x 7 cols
    private int currentPlayer = 1;

    public GameSession(ClientHandler p1, ClientHandler p2) {
        this.player1 = p1;
        this.player2 = p2;
    }

    @Override
    public void run() {
        player1.send("START RED");
        player2.send("START YELLOW");
    }

    public void receiveMessage(ClientHandler sender, String msg) {
        if (msg.startsWith("MOVE")) {
            int col = Integer.parseInt(msg.split(" ")[1]);
            int row = getAvailableRow(col);
            if (row != -1 && ((currentPlayer == 1 && sender == player1) || (currentPlayer == 2 && sender == player2))) {
                board[row][col] = currentPlayer;
                int playerId = (sender == player1) ? 1 : 2;
                player1.send("MOVE " + row + " " + col + " " + playerId);
                player2.send("MOVE " + row + " " + col + " " + playerId);
                currentPlayer = 3 - currentPlayer;
            }
        }
        else if (msg.startsWith("CHAT ")) {
            String chatMsg = msg.substring(5); // everything after "CHAT "
            player1.send("CHAT " + chatMsg);
            player2.send("CHAT " + chatMsg);
        }

    }

    private int getAvailableRow(int col) {
        for (int row = 5; row >= 0; row--) {
            if (board[row][col] == 0) return row;
        }
        return -1;
    }
}