import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// this class makes the server gui window
public class ServerGUI extends Application {
    private static TextArea logArea;

    // this is the real starting point for JavaFX
    public static void main(String[] args) {
        launch(args); // this is the actual JavaFX entry point
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connect Four Server"); // set window title

        logArea = new TextArea(); // text area to show server logs
        logArea.setEditable(false); // don't allow typing inside
        logArea.setWrapText(true); // wrap text nicely

        BorderPane root = new BorderPane(); // layout for gui
        root.setCenter(logArea); // put text area in center

        primaryStage.setScene(new Scene(root, 360, 300)); // set size of window
        primaryStage.show(); // show the window

        // start the server in a new thread
        new Thread(() -> ConnectFourServer.startServer()).start();
    }

    // this function to add log messages to the window
    public static void log(String msg) {
        Platform.runLater(() -> {
            logArea.appendText(msg + "\n");
        });
    }
}