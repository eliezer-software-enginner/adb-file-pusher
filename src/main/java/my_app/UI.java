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
     State<String> ftpStatus = new State<>("");
     State<String> ftpServer = new State<>("192.168.3.104");
     State<String> ftpPort = new State<>("2221");
     State<String> ftpUsername = new State<>("android");
     State<String> ftpPassword = new State<>("android");
     State<Integer> pushProgress = new State<>(0);
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
                .c_child(new Row(new RowProps().spacingOf(20))
                                .r_child(InputColumn("Destination folder", folderDestination))
                                .r_child(InputColumn("File path", currentFile))
                                .r_child(ActionButton("Upload to FTP", this::push))
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
        String filePath = currentFile.get();
        String destFolder = folderDestination.get();
        
        System.out.println("[FTP] Starting upload operation");
        System.out.println("[FTP] File path: " + (filePath != null ? "'" + filePath + "'" : "null"));
        System.out.println("[FTP] Destination folder: " + (destFolder != null ? "'" + destFolder + "'" : "null"));
        
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("[FTP] ✗ Validation failed: No file selected");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please select a file to upload");
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

        System.out.println("[FTP] ✓ Validation passed, starting upload");
        pushProgress.set(0);
        pushFinished = false;

        // 🔄 Thread do progress
        Thread.ofVirtual().start(() -> {
            System.out.println("[FTP] Progress thread started");
            int value = 0;

            while (!pushFinished && value < 90) {
                value += 1 + (int)(Math.random() * 2); // avanço irregular
                int safeValue = Math.min(value, 90);

                Platform.runLater(() ->
                        pushProgress.set(safeValue)
                );

                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}
            }
            System.out.println("[FTP] Progress thread finished");
        });

        // 🚀 Thread do upload FTP real
        Thread.ofVirtual().start(() -> {
            try {
                System.out.println("[FTP] Starting FTP upload...");
                
                FtpService.FtpResult result = ftpService.uploadFile(filePath.trim(), destFolder.trim());
                
                System.out.println("[FTP] Upload completed: " + result.toString());
                pushFinished = true;

                Platform.runLater(() -> {
                    pushProgress.set(100);
                    
                    Alert alert = new Alert(result.success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                    alert.setContentText(result.toString());
                    alert.setTitle(result.success ? "Success" : "Error");
                    alert.setHeaderText(null);
                    alert.show();
                    
                    if (result.success) {
                        ftpStatus.set("✅ Last upload successful");
                    } else {
                        ftpStatus.set("❌ Last upload failed");
                    }
                });

            } catch (Exception e) {
                System.out.println("[FTP] ✗ Exception occurred: " + e.getMessage());
                e.printStackTrace();
                pushFinished = true;
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("❌ Upload error: " + e.getMessage());
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.show();
                    
                    ftpStatus.set("❌ Upload error: " + e.getMessage());
                });
            }
        });
    }
}
