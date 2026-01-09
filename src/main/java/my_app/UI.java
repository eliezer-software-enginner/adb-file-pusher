package my_app;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.props.TextProps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UI {
     State<String> folderDestination = new State<>("");
     State<String> currentFile = new State<>("");
     State<String> devices = new State<>("");
     State<String> ipPort = new State<>("");
     State<String> pairCode = new State<>("");
     State<Integer> pushProgress = new State<>(0);
    private volatile boolean pushFinished = false;

    public Component render() {
        final var showProgress =
                pushProgress.map(v -> v > 0 && v < 100);

        return new Column(new ColumnProps().paddingAll(15).spacingOf(20))
                .c_child(new Text("ADB Pusher", new TextProps().fontSize(25)))
                .c_child(
                        new Row(new RowProps().spacingOf(40))
                                .r_child(
                                        new Column(new ColumnProps().spacingOf(10))
                                                .c_child(ActionButton("Find devices", this::findDevices))
                                                .c_child(new Text(devices, new TextProps().fontSize(17)))
                                ).r_child(
                                        new Column(new ColumnProps().spacingOf(10))
                                                .c_child(ActionButton("Pair device", this::pairDevice))
                                                .c_child(Input_("IP:PORT",ipPort))
                                                .c_child(Input_("XXXXXX",pairCode))
                                ))
                .c_child(new SpacerVertical(20))
                .c_child(new LineHorizontal())
                .c_child(new Row(new RowProps().spacingOf(20))
                                .r_child(InputColumn("Destination folder", folderDestination))
                                .r_child(InputColumn("File path", currentFile))
                                .r_child(ActionButton("Push to device", this::push))
                )
                .c_child(Show.when(showProgress, ()-> new ProgressBar(pushProgress)));
    }

    private Input Input_(String placeholder, State<String> inputState) {
        return new Input(inputState, new InputProps().fontSize(17).placeHolder(placeholder));
    }

    private Column InputColumn(String label, State<String> inputState) {
        return new Column()
                .c_child(new Text(label, new TextProps().fontSize(18)))
                .c_child(Input_(label, inputState));
    }

    private Button ActionButton(String text, Runnable callback) {
        return new Button(text,
                new ButtonProps()
                        .onClick(callback)
                        .bgColor("#5D8A9D")
                        .textColor("white")
                        .fontSize(20));
    }

    private void pairDevice() {
        System.out.println("pairDevice()");

        Thread.ofVirtual().start(() -> {
            try {
                ProcessBuilder pb =
                        new ProcessBuilder("adb", "pair", ipPort.get());
                
                // Redireciona stderr para stdout para capturar toda a saída
                pb.redirectErrorStream(true);

                Process process = pb.start();

                // 1️⃣ Envia o código de pareamento
                String code = pairCode.get();
                if (code != null && !code.trim().isEmpty()) {
                    process.getOutputStream()
                            .write((code + "\n").getBytes());
                    process.getOutputStream().flush();
                }
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
                    String result = output.toString();
                    
                    // Verifica sucesso primeiro (mesmo que tenha erros de protocolo)
                    if(result.contains("Successfully paired")) {
                        devices.set("Successfully paired!");
                    } 
                    // Só considera falha se não tiver sucesso e tiver falha explícita
                    else if(result.contains("Enter pairing code: ") || result.contains("failed")) {
                        devices.set("Pairing failed");
                    } 
                    // Se tiver erro de protocolo mas não falha explícita, mostra a saída
                    else if(result.contains("protocol fault")) {
                        devices.set("Pairing completed (check device)");
                    }
                    else {
                        devices.set(result);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    devices.set("Error: " + e.getMessage());
                });
            }
        });
    }

    private void push() {
        // Validar campos antes de iniciar
        String filePath = currentFile.get();
        String destFolder = folderDestination.get();
        
        if (filePath == null || filePath.trim().isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please select a file to push");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.show();
            });
            return;
        }
        
        if (destFolder == null || destFolder.trim().isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please enter destination folder");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.show();
            });
            return;
        }

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
                    Thread.sleep(220);
                } catch (InterruptedException ignored) {}
            }
        });

        // 🚀 Thread do adb push real
        Thread.ofVirtual().start(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "adb",
                        "push",
                        filePath.trim(),
                        "/storage/emulated/0/" + destFolder.trim()
                );
                
                // Redirecionar stderr para capturar erros
                pb.redirectErrorStream(true);

                Process process = pb.start();
                
                // Capturar saída do processo
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                int exitCode = process.waitFor();
                pushFinished = true;

                Platform.runLater(() -> {
                    pushProgress.set(100);
                    
                    if (exitCode == 0) {
                        IO.println("Push finalizado: " + output.toString());
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("✅ Push finished successfully!");
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.show();
                    } else {
                        IO.println("❌ Erro no push: " + output.toString());
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("❌ Push failed: " + output.toString());
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.show();
                    }
                });

            } catch (Exception e) {
                pushFinished = true;
                Platform.runLater(() -> {
                    IO.println("❌ Erro no push: " + e.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("❌ Error: " + e.getMessage());
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.show();
                });
            }
        });
    }

    private void findDevices(){
        devices.set("");

        System.out.println("find devices()");

        Thread.ofVirtual().start(()->{
            ProcessBuilder pb = new ProcessBuilder("adb", "devices");
            try {
                var process = pb.start();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                StringBuilder sb = new StringBuilder();
                final boolean[] hasDevice = {false};
                
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    sb.append(line).append("\n");
                    
                    // Verifica se há dispositivo (linha que não seja o cabeçalho "List of devices")
                    if (!line.startsWith("List of devices") && !line.trim().isEmpty()) {
                        hasDevice[0] = true;
                    }
                }

                IO.println("lines: " + sb.toString());

                process.waitFor();

                Platform.runLater(() ->{
                    IO.println(sb.toString());
                    if (!hasDevice[0]) {
                        devices.set("No devices");
                    } else {
                        devices.set(sb.toString());
                    }
                });

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
