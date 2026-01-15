/*
Kirtan Patel Nisarg Patel
We have developed a connect four game using client and server in java
 we have a simple login screena and create account buttons which will help user to have their
 unique username and passwords then we have single play option where the user can play with the AI
 and learn how to play then  we have play online option where the user can play with their friends
 online , we also have a show statistics screen where the user can see their win % , total game played and longest
 winning streak and we also cretaed a How to play screen where it has information to how to play the game and the things
 we have implemeted in our game
*/

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.HPos;

// for Image
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// for server
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.application.Platform;
import java.io.FileReader;
import java.io.FileWriter;

// from this the application starts it s the main clas
public class ConnectFourClient extends Application {
    // different button style
    private static final String BUTTON_NORMAL_STYLE = "-fx-background-color: #ffffff; " +
            "-fx-text-fill: black; " +
            "-fx-font-weight: bold;";

    private static final String BUTTON_HOVER_STYLE = "-fx-background-color: #1e5799; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold;";
    // variabe are declared for differnt purposes like user name message
    private Stage primaryStage;
    private String currentUsername;
    private boolean isPlayer1 = false;
    private TextArea chatArea;
    private TextField messageField;
    private List<String> chatHistory = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private User currentUser = null;
    private String aiDifficulty = "EASY";
    //for connecting server
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private boolean isMultiplayer = false;

    private boolean myTurn = false;

    // helsp to launc the app
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(" Welcome to the Connect Four Game! ");
        // helsp to load the users for mthe file and dispolay the login screen
        loadUsersFromFile();
        showLoginScreen();
        primaryStage.show();
    }

    // handkes user acounts
    private static class User {
        String username;
        String password;
        int gamesWon;
        int gamesPlayed;

        int currentWinStreak; // win strak
        int longestWinStreak;
        // constructr
        User(String username, String password) {
            // initialises everything
            this.username = username;
            this.password = password;
            this.gamesWon = 0;
            this.gamesPlayed = 0;
            this.currentWinStreak = 0;
            this.longestWinStreak = 0;
        }
    }

    // shows the login screen
    private void showLoginScreen() {
        StackPane root = new StackPane();

        // background for the login screen
        Image bgImage = new Image(getClass().getResourceAsStream("/images/login.png"));
        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(
                        100, 100, true, true, true, false
                )
        );
        root.setBackground(new Background(backgroundImage));


        // Login
        GridPane loginGrid = new GridPane();
        loginGrid.setPadding(new Insets(20));
        loginGrid.setVgap(15);
        loginGrid.setHgap(15);
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.45); " +
                        "-fx-padding: 25;"
        );
        // username and password buttons
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #DAA520;");
        TextField userField = new TextField();
        loginGrid.add(userLabel, 0, 1);
        loginGrid.add(userField, 1, 1);

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: red;");
        PasswordField passField = new PasswordField();
        loginGrid.add(passLabel, 0, 2);
        loginGrid.add(passField, 1, 2);
// login butto
        Button loginBtn = new Button("Login");
        Button createBtn = new Button("Create Account");

        loginBtn.setStyle(BUTTON_NORMAL_STYLE);
        createBtn.setStyle(BUTTON_NORMAL_STYLE);

        // button hover style
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(BUTTON_HOVER_STYLE));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(BUTTON_NORMAL_STYLE));

        createBtn.setOnMouseEntered(e -> createBtn.setStyle(BUTTON_HOVER_STYLE));
        createBtn.setOnMouseExited(e -> createBtn.setStyle(BUTTON_NORMAL_STYLE));

        HBox btnBox = new HBox(10, loginBtn, createBtn);
        btnBox.setAlignment(Pos.CENTER);
        loginGrid.add(btnBox, 0, 3, 2, 1);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        loginGrid.add(errorLabel, 0, 4, 2, 1);

        // Add login
        root.getChildren().add(loginGrid);

        // Buttons
        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both username and password.");
            } else {
                User user = users.stream()
                        .filter(u -> u.username.equals(username) && u.password.equals(password))
                        .findFirst()
                        .orElse(null);

                if (user != null) {
                    currentUser = user;
                    currentUsername = user.username;
                    showHomeScreen();
                } else {
                    errorLabel.setText("Invalid credentials.");
                }
            }
        });

        createBtn.setOnAction(e -> showRegisterScreen());

        //  scene
        Scene scene = new Scene(root, 1000, 625);

        primaryStage.setScene(scene);
    }
// shows the register screen


    private void showRegisterScreen() {
        GridPane registerGrid = new GridPane();
        registerGrid.setAlignment(Pos.CENTER);
        registerGrid.setHgap(10);
        registerGrid.setVgap(10);
        registerGrid.setPadding(new Insets(25));
        registerGrid.setStyle("-fx-background-color: linear-gradient(to bottom, #1e5799, #2989d8);");

        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        column1.setHalignment(HPos.CENTER);
        column2.setHalignment(HPos.CENTER);
        registerGrid.getColumnConstraints().addAll(column1, column2);
        // shows the register account screen

        Label titleLabel = new Label("Create Account");
        titleLabel.setStyle("-fx-font-size: 40; -fx-font-weight: bold;-fx-text-fill: white;");
        registerGrid.add(titleLabel, 0, 0, 2, 1);


        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmField = new PasswordField();

// sets the sizes
        usernameField.setPrefWidth(200);
        passwordField.setPrefWidth(200);
        confirmField.setPrefWidth(200);
        // labels for the fields
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Create Password:");
        Label confirmLabel = new Label("Confirm Password:");

        usernameLabel.setStyle("-fx-text-fill: white;");
        passwordLabel.setStyle("-fx-text-fill: white;");
        confirmLabel.setStyle("-fx-text-fill: white;");

        registerGrid.add(new Label("Username:"), 0, 1);
        registerGrid.add(usernameField, 1, 1);
        registerGrid.add(new Label("Create Password:"), 0, 2);
        registerGrid.add(passwordField, 1, 2);
        registerGrid.add(new Label("Confirm Password:"), 0, 3);
        registerGrid.add(confirmField, 1, 3);


        Button createBtn = new Button("Create");
        Button cancelBtn = new Button("Cancel");

        // consistent styling
        createBtn.setStyle(BUTTON_NORMAL_STYLE);
        cancelBtn.setStyle(BUTTON_NORMAL_STYLE);

        // hovering style
        createBtn.setOnMouseEntered(e -> createBtn.setStyle(BUTTON_HOVER_STYLE));
        createBtn.setOnMouseExited(e -> createBtn.setStyle(BUTTON_NORMAL_STYLE));

        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(BUTTON_HOVER_STYLE));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(BUTTON_NORMAL_STYLE));
        HBox buttonBox = new HBox(10, createBtn, cancelBtn);
        buttonBox.setAlignment(Pos.CENTER);
        registerGrid.add(buttonBox, 0, 4, 2, 1);

// a  button handler
        createBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String pass = passwordField.getText().trim();
            String confirm = confirmField.getText().trim();

            if (username.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                showAlert("Error", "All fields are required.");
                return;
            }
            // sees if the password is matched
            if (!pass.equals(confirm)) {
                showAlert("Error", "Both passwords don't match!");
                return;
            }

            boolean exists = users.stream().anyMatch(u -> u.username.equals(username));
            if (exists) {
                showAlert("Error", "Username already exists.");
                return;
            }
            // adds and saves the user
            users.add(new User(username, pass));
            saveUsersToFile();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Account created. Please log in.");
            alert.showAndWait();

            showLoginScreen();
        });


        cancelBtn.setOnAction(e -> showLoginScreen());


        primaryStage.setScene(new Scene(registerGrid, 600, 600));
    }


    private void showHomeScreen() {
        chatHistory.clear(); // Clear chat

        // vbox for the home screen
        VBox homeBox = new VBox(15);
        homeBox.setAlignment(Pos.CENTER);
        homeBox.setPadding(new Insets(25));
        homeBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1e5799, #2989d8);");
        // welcome meassage
        Label welcomeLabel = new Label("Welcome, " + currentUsername + "!");
        welcomeLabel.setStyle("-fx-font-size: 18;-fx-text-fill: white;");
        double winPercentage = currentUser .gamesPlayed > 0 ?
                (currentUser .gamesWon * 100.0) / currentUser .gamesPlayed : 0;
        // styel the statisticss labels
        Label statsLabel = new Label("Statistics:");
        statsLabel.setStyle("-fx-font-size: 16; -fx-text-fill: white;");
        Label playedLabel = new Label("Games Played: " + currentUser .gamesPlayed);
        Label winLabel = new Label("Games Won: " + currentUser .gamesWon);
        Label winPercentageLabel = new Label("Win %: " + String.format("%.1f%%", winPercentage));
        playedLabel.setStyle("-fx-text-fill: white;");
        winLabel.setStyle("-fx-text-fill: white;");
        winPercentageLabel.setStyle("-fx-text-fill: white;");
// Create buttons
        Button singlePlayerBtn = new Button("Single Player Game ! ");
        Button multiplayerBtn = new Button("Play Online");
        Button statsBtn = new Button("Show Statistics");
        Button howToPlayBtn = new Button("How to Play");
        Button logoutBtn = new Button("Logout");
        Button exitBtn = new Button("Exit Game");

        // Apply consistent styling
        Button[] buttons = {singlePlayerBtn, multiplayerBtn, statsBtn, howToPlayBtn, logoutBtn, exitBtn};
        for (Button btn : buttons) {
            btn.setStyle(BUTTON_NORMAL_STYLE);
            btn.setOnMouseEntered(e -> btn.setStyle(BUTTON_HOVER_STYLE));
            btn.setOnMouseExited(e -> btn.setStyle(BUTTON_NORMAL_STYLE));
        }


        //  button width
        double buttonWidth = 200;
        singlePlayerBtn.setPrefWidth(buttonWidth);
        multiplayerBtn.setPrefWidth(buttonWidth);
        statsBtn.setPrefWidth(buttonWidth);
        howToPlayBtn.setPrefWidth(buttonWidth);
        logoutBtn.setPrefWidth(buttonWidth);
        exitBtn.setPrefWidth(buttonWidth);
// handlers for btns
        singlePlayerBtn.setOnAction(e -> showSingleGameScreen());


        multiplayerBtn.setOnAction(event -> connectToMultiplayerServer());

        statsBtn.setOnAction(e -> showStatistics());
        howToPlayBtn.setOnAction(e -> showHowToPlayScreen());
        logoutBtn.setOnAction(e -> showLoginScreen());

// Exit button
        exitBtn.setOnAction(e -> {
            saveUsersToFile();
            Platform.exit();
            System.exit(0);
        });
        homeBox.getChildren().addAll(
                welcomeLabel,
                singlePlayerBtn,
                multiplayerBtn,
                statsBtn,howToPlayBtn,
                logoutBtn,exitBtn
        );

        // displays the home screen
        primaryStage.setScene(new Scene(homeBox, 600, 600));
    }

    //waiting screen
    private void showWaitingScreen() {
        VBox waitBox = new VBox(20);
        waitBox.setAlignment(Pos.CENTER);
        waitBox.setPadding(new Insets(25));

        waitBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1e5799, #2989d8);");
        Label statusLabel = new Label("Connected as: " + currentUsername);
        Label waitLabel = new Label("Waiting for opponent...");

        statusLabel.setStyle("-fx-text-fill: white;");
        waitLabel.setStyle("-fx-text-fill: white;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(BUTTON_NORMAL_STYLE);
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(BUTTON_HOVER_STYLE));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(BUTTON_NORMAL_STYLE));
// Cancel button get to the home screen
        cancelBtn.setOnAction(e -> {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            showHomeScreen();
        });

        waitBox.getChildren().addAll(statusLabel, waitLabel, cancelBtn);
        primaryStage.setScene(new Scene(waitBox, 600, 600));
    }
// Show difficulty selection fo the Ai part

    private void showSingleGameScreen() {
        showSinglePlayerDifficultyScreen();
    }
    // Connect to multiplayer using server
    private void connectToMultiplayerServer() {
        isMultiplayer = true;

        showWaitingScreen();

        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12345);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println(currentUsername);

                Platform.runLater(() -> showAlert("Success", "Connected to server. Waiting for opponent..."));
//hread for server messages
                listenerThread = new Thread(() -> {
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.startsWith("START")) {
                                String color = line.split(" ")[1];
                                Platform.runLater(() -> startOnlineGame(color));
                            } else if (line.startsWith("MOVE")) {
                                String[] parts = line.split(" ");
                                int row = Integer.parseInt(parts[1]);
                                int col = Integer.parseInt(parts[2]);
                                int movePlayer = Integer.parseInt(parts[3]);

                                Platform.runLater(() -> {
                                    if (movePlayer == currentPlayer) {
                                        makeMoveAt(row, col);
                                    } else {
                                        applyMoveFromOpponent(row, col);
                                    }
                                });
                            } else if (line.startsWith("CHAT ")) {
                                String chatMsg = line.substring(5);
                                Platform.runLater(() -> {
                                    chatArea.appendText(chatMsg + "\n");
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                listenerThread.start();

            } catch (IOException ex) {
                // if the commection is failed will print the error message
                Platform.runLater(() -> showAlert("Connection Failed", "Unable to reach server."));
            }
        }).start();
    }
    // AI makes a move
    private void makeAIMove() {
        int col;
        // eay move
        if (aiDifficulty.equals("EASY")) {
            col = getRandomMove();
        } else {
            col = getSmartMove();
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            handleLocalMove(col);
        }));
        timeline.play();
    }
    //  random move for easy AI
    private int getRandomMove() {
        List<Integer> validColumns = new ArrayList<>();
        for (int col = 0; col < COLS; col++) {
            if (boardState[0][col] == 0) {
                validColumns.add(col);
            }
        }
        if (validColumns.isEmpty()) return -1;
        return validColumns.get((int)(Math.random() * validColumns.size()));
    }
    // medium  AI move

    private int getSmartMove() {
        //  AI win
        for (int col = 0; col < COLS; col++) {
            int row = getNextEmptyRow(col);
            if (row == -1) continue;

            boardState[row][col] = 2;
            if (checkWin(row, col)) {
                boardState[row][col] = 0;// reset
                return col;
            }
            boardState[row][col] = 0;
        }

        // Blocks  win move
        for (int col = 0; col < COLS; col++) {
            int row = getNextEmptyRow(col);
            if (row == -1) continue;

            boardState[row][col] = 1;
            if (checkWin(row, col)) {
                boardState[row][col] = 0;
                return col;
            }
            boardState[row][col] = 0;
        }
//pick a random move
        return getRandomMove();
    }
    // Find next available row in the col
    private int getNextEmptyRow(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (boardState[row][col] == 0) {
                return row;
            }
        }
        return -1;
    }
    // online game

    private void startOnlineGame(String color) {
        boardState = new int[ROWS][COLS];
        currentPlayer = color.equals("RED") ? 1 : 2;
        isPlayer1 = currentPlayer == 1;
        isMultiplayer = true;
        myTurn = color.equals("RED"); // RED always starts

        BorderPane gamePane = new BorderPane();
        gamePane.setPadding(new Insets(15));

        // Player Info
        HBox playerInfo = new HBox(20);
        playerInfo.setAlignment(Pos.CENTER);
        Label player1 = new Label("You (" + color + ")");
        player1.setTextFill(color.equals("RED") ? Color.RED : Color.GOLD);
        playerInfo.getChildren().addAll(player1);
        gamePane.setTop(playerInfo);

        // Game Board
        GridPane board = createGameBoard();
        gamePane.setCenter(board);

        //  Chat Area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(300);
        chatArea.setWrapText(true);
        chatArea.setStyle("-fx-font-family: 'Segoe UI', Arial, sans-serif;");
        messageField = new TextField();
        Label chatLabel = new Label("Game Chat");
        chatLabel.setStyle("-fx-font-weight: bold;");
        messageField.setPromptText("Type a message.");
        // Send message on enter key
        messageField.setOnAction(e -> sendMessage());

        Button sendBtn = new Button("Send");
        sendBtn.setStyle(BUTTON_NORMAL_STYLE);
        sendBtn.setOnMouseEntered(e -> sendBtn.setStyle(BUTTON_HOVER_STYLE));
        sendBtn.setOnMouseExited(e -> sendBtn.setStyle(BUTTON_NORMAL_STYLE));
        sendBtn.setOnAction(e -> sendMessage());

        HBox chatInput = new HBox(10, messageField, sendBtn);
        chatInput.setAlignment(Pos.CENTER);
        chatInput.setPadding(new Insets(5, 0, 0, 0));
        //chat histor
        if (!chatHistory.isEmpty()) {
            for (String msg : chatHistory) {
                chatArea.appendText(msg + "\n");
            }
        }
        //game starts messge
        String systemMsg = " Game started! You are playing as " + color + ":";
        chatArea.appendText(systemMsg + "\n");

        VBox chatBox = new VBox(10, new Label("Chat"), chatArea, chatInput);
        chatBox.setPadding(new Insets(10));
        chatBox.setAlignment(Pos.CENTER);
        chatBox.setPrefWidth(250);

        gamePane.setRight(chatBox);

        // Quit Button
        VBox bottomBox = new VBox(20);
        bottomBox.setPadding(new Insets(15));
        bottomBox.setAlignment(Pos.CENTER);

        Button quitBtn = new Button("Quit Game");
        quitBtn.setPrefWidth(200);
        quitBtn.setStyle(BUTTON_NORMAL_STYLE);
        quitBtn.setOnMouseEntered(e -> quitBtn.setStyle(BUTTON_HOVER_STYLE));
        quitBtn.setOnMouseExited(e -> quitBtn.setStyle(BUTTON_NORMAL_STYLE));
        quitBtn.setOnAction(e -> showHomeScreen());

        bottomBox.getChildren().add(quitBtn);
        gamePane.setBottom(bottomBox);
        // game scene
        primaryStage.setScene(new Scene(gamePane, 800, 800));
    }
    // Sendss  chat message to server
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            String formattedMessage = currentUsername + ": " + message;

            out.println("CHAT " + formattedMessage);  // sends to server
            messageField.clear();
        }

    }
    // save the data
    private void saveUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("users.txt"))) {
            for (User user : users) {
                writer.println(user.username + "," + user.password + "," + user.gamesPlayed + "," + user.gamesWon+ ","
                        + user.longestWinStreak);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Loads user data
    private void loadUsersFromFile() {
        users.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    User user = new User(parts[0], parts[1]);
                    if (parts.length >= 4) {
                        user.gamesPlayed = Integer.parseInt(parts[2]);
                        user.gamesWon = Integer.parseInt(parts[3]);
                    }
                    if (parts.length >= 5) {  //Check if longest Streak in winning
                        user.longestWinStreak = Integer.parseInt(parts[4]);
                    }
                    users.add(user);
                }
            }
        } catch (IOException e) {

        }
    }
    //game board size
    private static final int ROWS = 6;
    private static final int COLS = 7;
    //    2D array
    private Circle[][] cells = new Circle[ROWS][COLS];
    private int[][] boardState = new int[ROWS][COLS]; // 0=empty, 1=Red, 2=Yellow
    private int currentPlayer = 1;

    //  game board UI
    private GridPane createGameBoard() {
        GridPane board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setHgap(5);
        board.setVgap(5);
        board.setStyle("-fx-background-color: #2c3e50; " +
                "-fx-border-color: #3498db; " +
                "-fx-border-width: 10px; " + "-fx-padding: 15px;");

        // container pane for each column to handle clicks
        for (int col = 0; col < COLS; col++) {
            final int column = col;
            VBox colPane = new VBox(5);
            colPane.setAlignment(Pos.CENTER);



            colPane.setOnMouseClicked(e -> {
                handleMove(column);
            });

            // circles  column
            for (int row = 0; row < ROWS; row++) {
                Circle cell = new Circle(30);
                cell.setFill(Color.LIGHTGRAY);
                cell.setStroke(Color.BLACK);
                cells[row][col] = cell;
                boardState[row][col] = 0;
                colPane.getChildren().add(cell);
            }


            board.add(colPane, col, 0);
        }


        return board;
    }
    // scene which has how t oplay things
    private void showHowToPlayScreen() {
        VBox instructionsBox = new VBox(20);
        instructionsBox.setAlignment(Pos.CENTER);
        instructionsBox.setPadding(new Insets(25));
        instructionsBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1e5799, #2989d8);");

        Label titleLabel = new Label("How to Play Connect Four!");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: BLACK;");

        // scrollable area for area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        // different instructions
        Label objectiveLabel = new Label("Objective:");
        objectiveLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        Label objectiveText = new Label("The objective of Connect Four is to be the first player to get four " +
                "of your colored discs in a row, either horizontally, vertically, or diagonally, on the game board. " +
                "This is achieved by dropping your discs into one of the columns on the board, where they will fall " +
                "to the bottommost available space. ");
        objectiveText.setStyle("-fx-text-fill: white;");
        objectiveText.setWrapText(true);

        Label gameplayLabel = new Label("Gameplay:");
        gameplayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        Label gameplayText = new Label("1. Players take turns dropping one disc at a time into a column.\n" +
                "2. The disc falls to the lowest available space in the column.\n" +
                "3. The game ends when a player connects four discs or when the board is full (draw).");
        gameplayText.setStyle("-fx-text-fill: white;");
        gameplayText.setWrapText(true);

        Label modesLabel = new Label("Game Modes:");
        modesLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        Label modesText = new Label("Single Player: Play against the computer(AI) at different difficulty levels likely Easy and Medium.\n" +
                "Multiplayer: Play against another player online.");
        modesText.setStyle("-fx-text-fill: white;");
        modesText.setWrapText(true);

        contentBox.getChildren().addAll(
                objectiveLabel, objectiveText,
                gameplayLabel, gameplayText,
                modesLabel, modesText
        );

        scrollPane.setContent(contentBox);
        // back to menu btn
        Button backBtn = new Button("Back to Menu");
        backBtn.setPrefWidth(200);
        backBtn.setStyle(BUTTON_NORMAL_STYLE);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(BUTTON_HOVER_STYLE));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(BUTTON_NORMAL_STYLE));
        backBtn.setOnAction(e -> showHomeScreen());

        instructionsBox.getChildren().addAll(titleLabel, scrollPane, backBtn);

        primaryStage.setScene(new Scene(instructionsBox, 600, 600));
    }
    //  single-player game
    private void startSinglePlayerGame(String difficulty) {
        this.aiDifficulty = difficulty;//AI difficulty
        isMultiplayer = false;
        boardState = new int[ROWS][COLS];
        currentPlayer = 1;

        BorderPane gamePane = new BorderPane();
        gamePane.setPadding(new Insets(15));

        //  difficulty and player info
        Label difficultyLabel = new Label("Difficulty: " + aiDifficulty);
        difficultyLabel.setStyle("-fx-text-fill: brown;");

        HBox playerInfo = new HBox(20);
        playerInfo.setAlignment(Pos.CENTER);
        Label player1 = new Label("You (Red)");
        Label player2 = new Label("AI (Yellow)");
        player1.setTextFill(Color.RED);
        player2.setTextFill(Color.GOLD);
        playerInfo.getChildren().addAll(difficultyLabel, player1, player2);
        gamePane.setTop(playerInfo);

        GridPane board = createGameBoard();
        gamePane.setCenter(board);
        // Quit button
        VBox bottomBox = new VBox(20);
        bottomBox.setPadding(new Insets(15));
        bottomBox.setAlignment(Pos.CENTER);

        Button quitBtn = new Button("Quit Game");
        quitBtn.setPrefWidth(200);
        //  hover style
        quitBtn.setStyle(BUTTON_NORMAL_STYLE);
        quitBtn.setOnMouseEntered(e -> quitBtn.setStyle(BUTTON_HOVER_STYLE));
        quitBtn.setOnMouseExited(e -> quitBtn.setStyle(BUTTON_NORMAL_STYLE));
        quitBtn.setOnAction(e -> showHomeScreen());

        bottomBox.getChildren().add(quitBtn);
        gamePane.setBottom(bottomBox);

        primaryStage.setScene(new Scene(gamePane, 600, 600));
    }
    //difficulty selection
    private void showSinglePlayerDifficultyScreen() {
        VBox difficultyBox = new VBox(20);
        difficultyBox.setAlignment(Pos.CENTER);
        difficultyBox.setPadding(new Insets(25));
        difficultyBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1e5799, #2989d8);");

        Label titleLabel = new Label("Select Difficulty");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: black;");

        Button easyBtn = new Button("Easy"); // easy
        Button mediumBtn = new Button("Medium"); // for medium
        Button backBtn = new Button("Back"); // bakc btn

        // button width
        double buttonWidth = 200;
        easyBtn.setPrefWidth(buttonWidth);
        mediumBtn.setPrefWidth(buttonWidth);
        backBtn.setPrefWidth(buttonWidth);
        easyBtn.setStyle(BUTTON_NORMAL_STYLE);
        easyBtn.setOnMouseEntered(e -> easyBtn.setStyle(BUTTON_HOVER_STYLE));
        easyBtn.setOnMouseExited(e -> easyBtn.setStyle(BUTTON_NORMAL_STYLE));
// Style hover
        mediumBtn.setStyle(BUTTON_NORMAL_STYLE);
        mediumBtn.setOnMouseEntered(e -> mediumBtn.setStyle(BUTTON_HOVER_STYLE));
        mediumBtn.setOnMouseExited(e -> mediumBtn.setStyle(BUTTON_NORMAL_STYLE));

        backBtn.setStyle(BUTTON_NORMAL_STYLE);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(BUTTON_HOVER_STYLE));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(BUTTON_NORMAL_STYLE));
        // different btn action dependin on eay or medium difficulty level
        easyBtn.setOnAction(e -> {
            startSinglePlayerGame("EASY");
        });

        mediumBtn.setOnAction(e -> {
            startSinglePlayerGame("MEDIUM");
        });

        backBtn.setOnAction(e -> showHomeScreen());

        difficultyBox.getChildren().addAll(titleLabel, easyBtn, mediumBtn, backBtn);
        primaryStage.setScene(new Scene(difficultyBox, 600, 600));
    }


    private void makeMoveAt(int row, int col) {
        // current player's move
        boardState[row][col] = currentPlayer;
        //cell color
        cells[row][col].setFill(currentPlayer == 1 ? Color.RED : Color.GOLD);
//move for wins the game
        if (checkWin(row, col)) {
            String result = (currentPlayer == 1 ? "Red" : "Yellow") + " Wins!";
            currentUser .gamesPlayed++;
            currentUser .gamesWon += (currentPlayer == 1 && isPlayer1) || (currentPlayer == 2 && !isPlayer1) ? 1 : 0;

            // Updates  win streak
            currentUser .currentWinStreak++;
            if (currentUser .currentWinStreak > currentUser .longestWinStreak) {
                currentUser .longestWinStreak = currentUser .currentWinStreak;
            }
//result screen
            if (isMultiplayer) {
                showMultiplayerResultScreen(result);
            } else {
                showSingleResultScreen(result);
            }
        } else if (isBoardFull()) {
            if (isMultiplayer) {
                showMultiplayerResultScreen("Draw!");
            } else {
                showSingleResultScreen("Draw!");
            }
            currentUser .gamesPlayed++;
            currentUser .currentWinStreak = 0; //  draw then reset
        } else {
            currentPlayer = 3 - currentPlayer;
        }
    }


    private void handleMove(int col) {
        // Sends move to server
        if (isMultiplayer) {
            out.println("MOVE " + col);
        } else {
            handleLocalMove(col);
        }
    }

    private void handleLocalMove(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (boardState[row][col] == 0) {
                boardState[row][col] = currentPlayer;
                cells[row][col].setFill(currentPlayer == 1 ? Color.RED : Color.GOLD);
//current player's move
                if (checkWin(row, col)) {
                    currentUser.gamesPlayed++;
                    if (currentPlayer == 1) {
                        currentUser.gamesWon++;
                        currentUser.currentWinStreak++;
                        if (currentUser.currentWinStreak > currentUser.longestWinStreak) {
                            currentUser.longestWinStreak = currentUser.currentWinStreak;
                        }
                    } else {
                        // Reset win streak if AI wins
                        currentUser.currentWinStreak = 0;
                    }
                    showSingleResultScreen((currentPlayer == 1 ? "You" : "AI") + " Win!");
                } else if (isBoardFull()) {
                    currentUser.gamesPlayed++;
                    currentUser.currentWinStreak = 0;
                    // If board is then  draw
                    showSingleResultScreen("Draw!");
                } else {
                    currentPlayer = 3 - currentPlayer;

                    if (currentPlayer == 2) {
                        makeAIMove();
                    }
                }
                break;
            }
        }
    }

    private boolean isBoardFull() {
        // checks if anything is empty
        for (int col = 0; col < COLS; col++) {
            if (boardState[0][col] == 0) return false;
        }
        return true;
    }

    private void applyMoveFromOpponent(int row, int col) {
        int opponent = isPlayer1 ? 2 : 1;
        // opponent move
        boardState[row][col] = opponent;
        cells[row][col].setFill(opponent == 1 ? Color.RED : Color.GOLD);
    }

    // checks for all 4 directions
    private boolean checkWin(int row, int col) {
        int player = boardState[row][col];
        return countDirection(row, col, 1, 0, player) + countDirection(row, col, -1, 0, player) > 2 || // vertical
                countDirection(row, col, 0, 1, player) + countDirection(row, col, 0, -1, player) > 2 || // horizontal
                countDirection(row, col, 1, 1, player) + countDirection(row, col, -1, -1, player) > 2 || // diagonal /
                countDirection(row, col, 1, -1, player) + countDirection(row, col, -1, 1, player) > 2;   // diagonal \
    }

    // methos which counts the direction
    private int countDirection(int row, int col, int dr, int dc, int player) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && boardState[r][c] == player) {
            count++;
            r += dr;
            c += dc;
        }
        return count;
    }
    //  game result method for single player

    private void showSingleResultScreen(String result) {
        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(25));
        resultBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1e5799, #2989d8);");

        Label resultLabel = new Label(result);
        resultLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");

        Button playAgain = new Button("Play Again");
        Button backHome = new Button("Back to Home");

        // Make both buttons the same width
        double buttonWidth = 200;
        playAgain.setPrefWidth(buttonWidth);
        backHome.setPrefWidth(buttonWidth);

        // hover styles
        playAgain.setStyle(BUTTON_NORMAL_STYLE);
        playAgain.setOnMouseEntered(e -> playAgain.setStyle(BUTTON_HOVER_STYLE));
        playAgain.setOnMouseExited(e -> playAgain.setStyle(BUTTON_NORMAL_STYLE));

        backHome.setStyle(BUTTON_NORMAL_STYLE);
        backHome.setOnMouseEntered(e -> backHome.setStyle(BUTTON_HOVER_STYLE));
        backHome.setOnMouseExited(e -> backHome.setStyle(BUTTON_NORMAL_STYLE));

        playAgain.setOnAction(e -> showSingleGameScreen());
        backHome.setOnAction(e -> showHomeScreen());

        resultBox.getChildren().addAll(resultLabel, playAgain, backHome);
        primaryStage.setScene(new Scene(resultBox, 600, 600));
    }
    //mwthod for the multy player sscreen
    private void showMultiplayerResultScreen(String result) {
        VBox resultBox = new VBox(20);
        resultBox.setAlignment(Pos.CENTER);
        resultBox.setPadding(new Insets(25));
        resultBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1e5799, #2989d8);");
        Label resultLabel = new Label(result);// Shows result
        resultLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;-fx-text-fill: white;");
        // btn for the paly again and go to the home screen
        Button playAgain = new Button("Play Again");
        Button backHome = new Button("Back to Home");

        double buttonWidth = 200;
        playAgain.setPrefWidth(buttonWidth);
        backHome.setPrefWidth(buttonWidth);

        // hover styles for the btn
        playAgain.setStyle(BUTTON_NORMAL_STYLE);
        playAgain.setOnMouseEntered(e -> playAgain.setStyle(BUTTON_HOVER_STYLE));
        playAgain.setOnMouseExited(e -> playAgain.setStyle(BUTTON_NORMAL_STYLE));

        backHome.setStyle(BUTTON_NORMAL_STYLE);
        backHome.setOnMouseEntered(e -> backHome.setStyle(BUTTON_HOVER_STYLE));
        backHome.setOnMouseExited(e -> backHome.setStyle(BUTTON_NORMAL_STYLE));

        playAgain.setOnAction(e -> connectToMultiplayerServer());
        backHome.setOnAction(e -> showHomeScreen());

        resultBox.getChildren().addAll(resultLabel, playAgain, backHome);
        primaryStage.setScene(new Scene(resultBox, 600, 600));
    }
    //  methid which disaplays the statistics method
    private void showStatistics() {
        VBox statsBox = new VBox(15);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(25));
        statsBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1e5799, #2989d8);");
// Calculate win percentage
        double winPercentage = currentUser.gamesPlayed > 0 ?
                (currentUser.gamesWon * 100.0) / currentUser.gamesPlayed : 0;

        Label statsLabel = new Label("Game Statistics");
        statsLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
// Shows total games played
        Label playedLabel = new Label("Games Played: " + currentUser.gamesPlayed);
        Label winLabel = new Label("Win %: " + String.format("%.1f%%", winPercentage));
        // Shows longest win streak
        Label streakLabel = new Label("Longest Win Streak: "+ currentUser.longestWinStreak);
        playedLabel.setStyle("-fx-text-fill: white;");
        winLabel.setStyle("-fx-text-fill: white;");
        streakLabel.setStyle("-fx-text-fill: white;");

        Button backBtn = new Button("Back to Home");
        backBtn.setPrefWidth(200);

        //  hover style
        backBtn.setStyle(BUTTON_NORMAL_STYLE);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(BUTTON_HOVER_STYLE));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(BUTTON_NORMAL_STYLE));

        backBtn.setOnAction(e -> showHomeScreen());

        statsBox.getChildren().addAll(
                statsLabel, playedLabel, winLabel, streakLabel, backBtn
        );
        primaryStage.setScene(new Scene(statsBox, 600, 600));
    }


    // if there is any error then this will pop up
    private void showAlert(String title, String message) {
        Alert alert;
        if (title.equals("Success")) {
            alert = new Alert(Alert.AlertType.INFORMATION);
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/check.png")));
            icon.setFitHeight(48);
            icon.setFitWidth(48);
            alert.setGraphic(icon);
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}