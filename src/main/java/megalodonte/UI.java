package megalodonte;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class UI {
    State<String> folderDestination = new State<>("videos");
    State<String> currentFile = new State<>("/home/eliezer/1_teste.mp4");
    State<String> devices = new State<>("");

    State<String> ipPort = new State<>("192.168.3.111:44405");
    State<String> pairCode = new State<>("378800");

    State<Integer> pushProgress = new State<>(0);
    private volatile boolean pushFinished = false;

    public Component render() {
        return new Column(new ColumnProps().paddingAll(15).spacingOf(20))
                .child(new Text("ADB Pusher", new TextProps().fontSize(25)))
                .child(
                        new Row(new RowProps().spacingOf(40))
                                .child(
                                        new Column()
                                                .child(new Button("Find devices", new ButtonProps().onClick(this::findDevices)))
                                                .child(new Text(devices))
                                ).child(
                                        new Column()
                                                .child(new Button("Pair device", new ButtonProps().onClick(this::pairDevice)))
                                                .child(new Input(ipPort))
                                                .child(new Input(pairCode))
                                ))
                .child(new SpacerVertical(20))
                .child(new LineHorizontal())
                .child(
                        new Row().child(
                            new Column()
                                    .child(new Text("Pasta de destino"))
                                .child(new Input(folderDestination))
                        ).child(
                        new Column().child(new Text("Caminho do arquivo"))
                                .child(new Input(currentFile)))
                                .child(new Button("Push to device", new ButtonProps().onClick(this::push)))

                )
                .child(new ProgressBar(pushProgress));

    }

    private void pairDevice() {
        Thread.ofVirtual().start(() -> {
            try {
                ProcessBuilder pb =
                        new ProcessBuilder("adb", "pair", ipPort.get());

                Process process = pb.start();

                // 1️⃣ Envia o código de pareamento
                process.getOutputStream()
                        .write((pairCode.get() + "\n").getBytes());
                process.getOutputStream().flush();
                process.getOutputStream().close();

                // 2️⃣ Lê a resposta
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(process.getInputStream())
                        );

                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                process.waitFor();

                Platform.runLater(() -> {
                    IO.println(output.toString());
                    devices.set(output.toString());
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    private void push() {
        pushProgress.set(0);
        pushFinished = false;

        // 🔄 Thread do fake progress
        Thread.ofVirtual().start(() -> {
            int value = 0;

            while (!pushFinished && value < 90) {
                value += 1 + (int)(Math.random() * 3); // avanço irregular
                int safeValue = Math.min(value, 90);

                Platform.runLater(() ->
                        pushProgress.set(safeValue)
                );

                try {
                    //Thread.sleep(120); // suavidade
                    Thread.sleep(180);
                } catch (InterruptedException ignored) {}
            }
        });

        // 🚀 Thread do adb push real
        Thread.ofVirtual().start(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "adb",
                        "push",
                        currentFile.get(),
                        "/storage/emulated/0/" + folderDestination.get()
                );

                Process process = pb.start();
                process.waitFor();

                pushFinished = true;

                Platform.runLater(() -> {
                    pushProgress.set(100);
                    IO.println("✅ Push finalizado");
                    Alert a = new Alert(Alert.AlertType.WARNING);
                                a.setContentText("✅ Push finalizado");
                                a.setTitle(null);
                                a.setHeaderText(null);
                                a.setGraphic(null);
                                a.show();
                });

            } catch (Exception e) {
                pushFinished = true;
                Platform.runLater(() ->
                        IO.println("❌ Erro no push: " + e.getMessage())
                );
            }
        });
    }


    private void findDevices(){
        devices.set("");

        Thread.ofVirtual().start(()->{
            ProcessBuilder pb = new ProcessBuilder("adb", "devices");
            try {
                var process = pb.start();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue; // 👈 aqui resolve
                    sb.append(line).append("\n");
                }

                process.waitFor();

                Platform.runLater(() ->{
                    IO.println(sb.toString());
                    devices.set(sb.toString());
                });

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
