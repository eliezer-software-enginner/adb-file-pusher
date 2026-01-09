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
        System.out.println("[PAIR] Starting pair device operation");
        String targetIp = ipPort.get();
        String code = pairCode.get();
        
        System.out.println("[PAIR] Target IP: " + targetIp);
        System.out.println("[PAIR] Pairing code provided: " + (code != null && !code.trim().isEmpty() ? "YES" : "NO"));

        Thread.ofVirtual().start(() -> {
            try {
                System.out.println("[PAIR] Executing: adb pair " + targetIp);
                ProcessBuilder pb =
                        new ProcessBuilder("adb", "pair", targetIp);
                
                // Redireciona stderr para stdout para capturar toda a saída
                pb.redirectErrorStream(true);

                Process process = pb.start();
                System.out.println("[PAIR] Process started, PID: " + process.pid());

                // 1️⃣ Envia o código de pareamento
                if (code != null && !code.trim().isEmpty()) {
                    System.out.println("[PAIR] Sending pairing code: " + code);
                    process.getOutputStream()
                            .write((code + "\n").getBytes());
                    process.getOutputStream().flush();
                } else {
                    System.out.println("[PAIR] No pairing code provided, closing output stream");
                }
                process.getOutputStream().close();

                // 2️⃣ Lê a resposta
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(process.getInputStream())
                        );

                String line;
                StringBuilder output = new StringBuilder();
                System.out.println("[PAIR] Reading process output:");
                
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    System.out.println("[PAIR] OUTPUT: " + line);
                }

                int exitCode = process.waitFor();
                System.out.println("[PAIR] Process finished with exit code: " + exitCode);
                System.out.println("[PAIR] Full output: " + output.toString());

                Platform.runLater(() -> {
                    IO.println(output.toString());
                    String result = output.toString();
                    
                    // Verifica sucesso primeiro (mesmo que tenha erros de protocolo)
                    if(result.contains("Successfully paired")) {
                        System.out.println("[PAIR] ✓ Pairing successful!");
                        devices.set("Successfully paired!");
                    } 
                    // Só considera falha se não tiver sucesso e tiver falha explícita
                    else if(result.contains("Enter pairing code: ") || result.contains("failed")) {
                        System.out.println("[PAIR] ✗ Pairing failed");
                        devices.set("Pairing failed");
                    } 
                    // Se tiver erro de protocolo mas não falha explícita, mostra a saída
                    else if(result.contains("protocol fault")) {
                        System.out.println("[PAIR] ⚠ Protocol fault detected, but may have succeeded");
                        devices.set("Pairing completed (check device)");
                    }
                    else {
                        System.out.println("[PAIR] ? Unknown result");
                        devices.set(result);
                    }
                });

            } catch (Exception e) {
                System.out.println("[PAIR] ✗ Exception occurred: " + e.getMessage());
                e.printStackTrace();
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
        
        System.out.println("[PUSH] Starting push operation");
        System.out.println("[PUSH] File path: " + (filePath != null ? "'" + filePath + "'" : "null"));
        System.out.println("[PUSH] Destination folder: " + (destFolder != null ? "'" + destFolder + "'" : "null"));
        
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("[PUSH] ✗ Validation failed: No file selected");
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
            System.out.println("[PUSH] ✗ Validation failed: No destination folder");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please enter destination folder");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.show();
            });
            return;
        }

        System.out.println("[PUSH] ✓ Validation passed, starting operation");
        pushProgress.set(0);
        pushFinished = false;

        // 🔄 Thread do fake progress
        Thread.ofVirtual().start(() -> {
            System.out.println("[PUSH] Progress thread started");
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
            System.out.println("[PUSH] Progress thread finished");
        });

        // 🚀 Thread do adb push real
        Thread.ofVirtual().start(() -> {
            try {
                String fullDestPath = "/storage/emulated/0/" + destFolder.trim();
                String command = "adb push \"" + filePath.trim() + "\" \"" + fullDestPath + "\"";
                
                System.out.println("[PUSH] Executing: " + command);
                ProcessBuilder pb = new ProcessBuilder(
                        "adb",
                        "push",
                        filePath.trim(),
                        fullDestPath
                );
                
                // Redirecionar stderr para capturar erros
                pb.redirectErrorStream(true);

                Process process = pb.start();
                System.out.println("[PUSH] Process started, PID: " + process.pid());
                
                // Capturar saída do processo
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                
                StringBuilder output = new StringBuilder();
                String line;
                int lineCount = 0;
                
                System.out.println("[PUSH] Reading process output:");
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    output.append(line).append("\n");
                    System.out.println("[PUSH] Line " + lineCount + ": " + line);
                }
                
                int exitCode = process.waitFor();
                System.out.println("[PUSH] Process finished with exit code: " + exitCode);
                System.out.println("[PUSH] Total lines read: " + lineCount);
                pushFinished = true;

                Platform.runLater(() -> {
                    pushProgress.set(100);
                    
                    if (exitCode == 0) {
                        System.out.println("[PUSH] ✓ Push successful!");
                        IO.println("Push finalizado: " + output.toString());
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("✅ Push finished successfully!");
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.show();
                    } else {
                        System.out.println("[PUSH] ✗ Push failed with exit code: " + exitCode);
                        IO.println("❌ Erro no push: " + output.toString());
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("❌ Push failed: " + output.toString());
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.show();
                    }
                });

            } catch (Exception e) {
                System.out.println("[PUSH] ✗ Exception occurred: " + e.getMessage());
                e.printStackTrace();
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
        System.out.println("[DEVICES] Starting find devices operation");

        Thread.ofVirtual().start(()->{
            try {
                System.out.println("[DEVICES] Executing: adb devices");
                ProcessBuilder pb = new ProcessBuilder("adb", "devices");
                var process = pb.start();
                System.out.println("[DEVICES] Process started, PID: " + process.pid());

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                StringBuilder sb = new StringBuilder();
                final boolean[] hasDevice = {false};
                int lineCount = 0;
                
                System.out.println("[DEVICES] Reading process output:");
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    if (line.isBlank()) {
                        System.out.println("[DEVICES] Line " + lineCount + ": [blank]");
                        continue;
                    }
                    sb.append(line).append("\n");
                    System.out.println("[DEVICES] Line " + lineCount + ": " + line);
                    
                    // Verifica se há dispositivo (linha que não seja o cabeçalho "List of devices")
                    if (!line.startsWith("List of devices") && !line.trim().isEmpty()) {
                        hasDevice[0] = true;
                        System.out.println("[DEVICES] ✓ Device found on line " + lineCount);
                    }
                }

                int exitCode = process.waitFor();
                System.out.println("[DEVICES] Process finished with exit code: " + exitCode);
                System.out.println("[DEVICES] Total lines read: " + lineCount);
                System.out.println("[DEVICES] Has devices: " + hasDevice[0]);
                IO.println("lines: " + sb.toString());

                Platform.runLater(() ->{
                    IO.println(sb.toString());
                    if (!hasDevice[0]) {
                        System.out.println("[DEVICES] → No devices found");
                        devices.set("No devices");
                    } else {
                        System.out.println("[DEVICES] → Devices found");
                        devices.set(sb.toString());
                    }
                });

            } catch (IOException | InterruptedException e) {
                System.out.println("[DEVICES] ✗ Exception occurred: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }
}
