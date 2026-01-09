package my_app;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import megalodonte.*;
import megalodonte.components.*;
import megalodonte.components.inputs.Input;
import megalodonte.components.inputs.TextAreaInput;
import megalodonte.props.TextProps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class UI {
     State<String> folderDestination = new State<>("");
     State<String> currentFile = new State<>("");
     State<String> ftpStatus = new State<>("");
     State<String> ftpServer = new State<>("192.168.3.104");
     State<String> ftpPort = new State<>("2221");
     State<String> ftpUsername = new State<>("android");
     State<String> ftpPassword = new State<>("android");
     State<Integer> pushProgress = new State<>(0);
     State<String> uploadResults = new State<>("");
    private volatile boolean pushFinished = false;
    private FtpService ftpService;

    public Component render() {
        final var showProgress =
                pushProgress.map(v -> v > 0 && v < 100);

        // Initialize FTP service when component renders
        if (ftpService == null) {
            updateFtpService();
        }

        return new Column(new ColumnProps().paddingAll(15).spacingOf(20))
                .c_child(new Text("FTP File Pusher", new TextProps().fontSize(25)))
                .c_child(
                        new Column(new ColumnProps().spacingOf(10))
                                .c_child(new Text("FTP Configuration", new TextProps().fontSize(18)))
                                .c_child(new Row(new RowProps().spacingOf(10))
                                        .r_child(InputColumn("Server", ftpServer))
                                        .r_child(InputColumn("Port", ftpPort))
                                )
                                .c_child(new Row(new RowProps().spacingOf(10))
                                        .r_child(InputColumn("Username", ftpUsername))
                                        .r_child(InputColumn("Password", ftpPassword))
                                )
                                .c_child(ActionButton("Test Connection", this::testFtpConnection))
                                .c_child(new Text(ftpStatus, new TextProps().fontSize(15)))
                )
                .c_child(new SpacerVertical(20))
                .c_child(new LineHorizontal())
                .c_child(
                        new Column(new ColumnProps().spacingOf(10))
                                .c_child(new Text("Files to Upload", new TextProps().fontSize(18)))
                                .c_child(TextAreaInput_("One file path per line", currentFile))
                                .c_child(new Text("Files found: " + getFileCount(), new TextProps().fontSize(14)))
                )
                .c_child(InputColumn("Destination folder", folderDestination))
                .c_child(ActionButton("Upload to FTP", this::push))
                .c_child(Show.when(showProgress, () -> new ProgressBar(pushProgress)))
                .c_child(new LineHorizontal())
                .c_child(
                        new Column(new ColumnProps().spacingOf(5))
                                .c_child(new Text("Upload Results", new TextProps().fontSize(16)))
                                .c_child(TextAreaInput_("Results will appear here...", uploadResults))
                );
    }

    private Input Input_(String placeholder, State<String> inputState) {
        return new Input(inputState, new InputProps().fontSize(17).placeHolder(placeholder));
    }
    
    private TextAreaInput TextAreaInput_(String placeholder, State<String> inputState) {
        return new TextAreaInput(inputState, new InputProps().fontSize(14).placeHolder(placeholder));
    }
    
    private int getFileCount() {
        String files = currentFile.get();
        if (files == null || files.trim().isEmpty()) return 0;
        
        String[] lines = files.split("\n");
        return (int) Arrays.stream(lines)
                .filter(line -> line != null && !line.trim().isEmpty())
                .count();
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

    private void updateFtpService() {
        String server = ftpServer.get();
        String portStr = ftpPort.get();
        String username = ftpUsername.get();
        String password = ftpPassword.get();
        
        if (server != null && portStr != null && username != null && password != null) {
            try {
                int port = Integer.parseInt(portStr.trim());
                ftpService = new FtpService(server.trim(), port, username.trim(), password.trim());
                System.out.println("[FTP] Service updated: " + server + ":" + port);
            } catch (NumberFormatException e) {
                System.out.println("[FTP] Invalid port number: " + portStr);
                ftpService = null;
}
    }
}
    
    private void testFtpConnection() {
        System.out.println("[FTP] Testing connection...");
        updateFtpService();
        
        if (ftpService == null) {
            Platform.runLater(() -> {
                ftpStatus.set("❌ Invalid FTP configuration");
            });
            return;
        }
        
        Thread.ofVirtual().start(() -> {
            try {
                boolean success = ftpService.testConnection();
                Platform.runLater(() -> {
                    ftpStatus.set(success ? "✅ Connection successful!" : "❌ Connection failed");
                });
            } catch (Exception e) {
                System.out.println("[FTP] Connection test error: " + e.getMessage());
                Platform.runLater(() -> {
                    ftpStatus.set("❌ Error: " + e.getMessage());
                });
            }
        });
    }

    private void push() {
        // Validar campos antes de iniciar
        String filesText = currentFile.get();
        String destFolder = folderDestination.get();
        
        // Extrair lista de arquivos
        String[] filePaths = extractFilePaths(filesText);
        
        System.out.println("[FTP] Starting batch upload operation");
        System.out.println("[FTP] Files count: " + filePaths.length);
        System.out.println("[FTP] Destination folder: " + (destFolder != null ? "'" + destFolder + "'" : "null"));
        
        if (filePaths.length == 0) {
            System.out.println("[FTP] ✗ Validation failed: No files found");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please enter at least one file path");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.show();
            });
            return;
        }
        
        if (destFolder == null || destFolder.trim().isEmpty()) {
            System.out.println("[FTP] ✗ Validation failed: No destination folder");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please enter destination folder");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.show();
            });
            return;
        }

        // Verificar configuração FTP
        updateFtpService();
        if (ftpService == null) {
            System.out.println("[FTP] ✗ Validation failed: Invalid FTP configuration");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid FTP configuration");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.show();
            });
            return;
        }

        System.out.println("[FTP] ✓ Validation passed, starting batch upload");
        pushProgress.set(0);
        pushFinished = false;
        uploadResults.set("");

        // 🚀 Thread do upload em lote
        Thread.ofVirtual().start(() -> {
StringBuilder results = new StringBuilder();
        final int[] successCount = {0};
        final int totalFiles = filePaths.length;
            
            for (int i = 0; i < totalFiles; i++) {
                String filePath = filePaths[i].trim();
                String fileName = new java.io.File(filePath).getName();
                
                System.out.println("[FTP] Uploading file " + (i + 1) + "/" + totalFiles + ": " + fileName);
                
                try {
                    FtpService.FtpResult result = ftpService.uploadFile(filePath, destFolder.trim());
                    
                    // Atualizar progress
                    int progress = (int) ((i + 1.0) / totalFiles * 100);
                    Platform.runLater(() -> pushProgress.set(progress));
                    
                    // Adicionar resultado
                    String resultText = String.format("%d. %s: %s%n", 
                        (i + 1), fileName, result.toString());
                    results.append(resultText);
                    
                    Platform.runLater(() -> {
                        uploadResults.set(results.toString());
                    });
                    
                    if (result.success) {
                        successCount[0]++;
                        System.out.println("[FTP] ✓ Upload successful: " + fileName);
                    } else {
                        System.out.println("[FTP] ✗ Upload failed: " + fileName + " - " + result.message);
                    }
                    
                } catch (Exception e) {
                    System.out.println("[FTP] ✗ Exception uploading " + fileName + ": " + e.getMessage());
                    String errorText = String.format("%d. %s: ❌ Error: %s%n", 
                        (i + 1), fileName, e.getMessage());
                    results.append(errorText);
                    
                    Platform.runLater(() -> {
                        uploadResults.set(results.toString());
                    });
                }
            }
            
            pushFinished = true;
            Platform.runLater(() -> {
                pushProgress.set(100);
                
                // Resumo final
                String summary = String.format("%n=== Upload Summary ===%n" +
                    "Total files: %d%n" +
                    "Successful: %d%n" +
                    "Failed: %d%n", 
                    totalFiles, successCount[0], totalFiles - successCount[0]);
                results.append(summary);
                uploadResults.set(results.toString());
                
                // Alert com resumo
                Alert alert = new Alert(successCount[0] == totalFiles ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
                alert.setContentText(String.format("Upload completed! %d/%d files successful", successCount[0], totalFiles));
                alert.setTitle("Batch Upload Complete");
                alert.setHeaderText(null);
                alert.show();
                
                if (successCount[0] == totalFiles) {
                    ftpStatus.set("✅ All files uploaded successfully");
                } else if (successCount[0] > 0) {
                    ftpStatus.set("⚠️ " + successCount[0] + "/" + totalFiles + " files uploaded");
                } else {
                    ftpStatus.set("❌ All uploads failed");
                }
            });
        });
    }
    
    private String[] extractFilePaths(String filesText) {
        if (filesText == null || filesText.trim().isEmpty()) {
            return new String[0];
        }
        
        return Arrays.stream(filesText.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toArray(String[]::new);
    }
}
