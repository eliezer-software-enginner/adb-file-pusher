package megalodonte;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {
    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Adb file pusher");
        stage.setResizable(false);
        final var root = new VBox(new UI().render().getNode());

        stage.setScene(new Scene(root, 600, 500));
        stage.show();
    }
}
