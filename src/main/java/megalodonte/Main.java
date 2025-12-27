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

        State<String> inputPathFolderState = new State<>("videos");
        State<String> currentFile = new State<>("");

        ComputedState<String> label =
                ComputedState.of(
                        () -> "O seu arquivo será copiado para '/storage/emulated/0/" + inputPathFolderState.get(),
                        inputPathFolderState
                );

        var column = new Column()
                .child(new Text(label))
                .child(new Input(inputPathFolderState))
                .child(new Input(currentFile))
                .child(new megalodonte.Button("Gerar comando adb", new ButtonProps().onClick(
                        () -> {
                            String comando = "db push '%s' '/storage/emulated/0/%s".formatted(currentFile.get(), inputPathFolderState.get());
                            IO.println(comando);

                            Alert a = new Alert(Alert.AlertType.WARNING);

                            a.setContentText("Copiou com sucesso");
                            a.setTitle(null);
                            a.setHeaderText(null);
                            a.setGraphic(null);
                            a.show();
                        }
                )));


        final var root = new VBox(column.getNode());

        //TextField inputPastaDestino = new TextField("videos");
        //TextField inputFilePath = new TextField("/home/eliezer/2025-12-26 10-41-30.mp4");
        //Button btnGerarComando = new Button("Gerar comando adb");

//        btnGerarComando.setOnMouseClicked(ev->{
//           final var s = inputFilePath.getText();//-> /home/eliezer/2025-12-26 10-41-30.mp4
//           //alvo-> db push '/home/eliezer/2025-12-26 10-41-30.mp4' '/storage/emulated/0/videos'
//            String comando = "db push '%s' '/storage/emulated/0/%s".formatted(s,inputPastaDestino.getText());
//            IO.println(comando);
//
//            Clipboard.setString(comando);
//
//            Alert a = new Alert(Alert.AlertType.WARNING);
//
//            a.setContentText("Copiou com sucesso");
//            a.setTitle(null);
//            a.setHeaderText(null);
//            a.setGraphic(null);
//            a.show();
//        });
//
//        root.getChildren().addAll(inputPastaDestino, text, inputFilePath, btnGerarComando);

        stage.setScene(new Scene(root, 600, 500));
        stage.show();
    }
}
