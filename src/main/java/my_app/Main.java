package my_app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import megalodonte.previewer.PreviewRoot;
import my_app.hotreload.CoesionApp;
import my_app.hotreload.HotReload;

import java.util.HashSet;
import java.util.Set;

@CoesionApp
public class Main extends Application {
    public static Stage stage;
    HotReload hotReload;
    boolean devMode = true;
    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        initializeScene(primaryStage);

        Set<String> exclusions = new HashSet<String>();
        exclusions.add("my_app.hotreload.CoesionApp");
        exclusions.add("my_app.hotreload.Reloader");

        if(devMode){
            this.hotReload = new HotReload(
                    "src/main/java/my_app",
                    "build/classes/java/main",
                    "build/resources/main",
                    "my_app.hotreload.UIReloaderImpl",
                    primaryStage,
                    exclusions
            );
            this.hotReload.start();
        }

        stage.show();

    }

    public static void initializeScene(Stage stage) throws Exception {
        stage.setTitle("Adb file pusher");
        stage.setResizable(false);

        final var root = new VBox(new UI().render().getNode());

        stage.setScene(new Scene(root, 600, 500));
        System.out.println("[App] Scene re-initialized.");
    }
}
