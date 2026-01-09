package my_app;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Serviço FTP para transferência de arquivos para Android
 */
public class FtpService {
    
    private final String server;
    private final int port;
    private final String username;
    private final String password;
    
    public FtpService(String server, int port, String username, String password) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Testa conexão com servidor FTP
     */
    public boolean testConnection() {
        FTPClient ftpClient = new FTPClient();
        try {
            System.out.println("[FTP] Testing connection to " + server + ":" + port);
            
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("[FTP] Connection failed - Reply code: " + replyCode);
                return false;
            }
            
            boolean loggedIn = ftpClient.login(username, password);
            if (!loggedIn) {
                System.out.println("[FTP] Login failed");
                return false;
            }
            
            System.out.println("[FTP] Connection successful!");
            ftpClient.logout();
            ftpClient.disconnect();
            return true;
            
        } catch (IOException e) {
            System.out.println("[FTP] Connection error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Envia arquivo via FTP
     */
    public FtpResult uploadFile(String localFilePath, String remoteFolder) {
        FTPClient ftpClient = new FTPClient();
        
        try {
            System.out.println("[FTP] Starting upload: " + localFilePath + " -> " + remoteFolder);
            
            // Conectar ao servidor
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                return new FtpResult(false, "Connection failed. Reply code: " + replyCode, null);
            }
            
            // Login
            if (!ftpClient.login(username, password)) {
                return new FtpResult(false, "Login failed", null);
            }
            
            // Configurar modo de transferência
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            
            // Mudar para o diretório remoto (criar se não existir)
            if (!changeToRemoteDirectory(ftpClient, remoteFolder)) {
                return new FtpResult(false, "Failed to access/create remote folder: " + remoteFolder, null);
            }
            
            // Obter nome do arquivo
            String fileName = new java.io.File(localFilePath).getName();
            
            // Enviar arquivo
            try (FileInputStream inputStream = new FileInputStream(localFilePath)) {
                boolean uploaded = ftpClient.storeFile(fileName, inputStream);
                
                if (uploaded) {
                    String remotePath = remoteFolder + "/" + fileName;
                    System.out.println("[FTP] Upload successful: " + remotePath);
                    return new FtpResult(true, "File uploaded successfully", remotePath);
                } else {
                    String error = ftpClient.getReplyString();
                    return new FtpResult(false, "Upload failed: " + error, null);
                }
            }
            
        } catch (IOException e) {
            System.out.println("[FTP] Upload error: " + e.getMessage());
            return new FtpResult(false, "Upload error: " + e.getMessage(), null);
        } finally {
            // Limpar conexão
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                System.out.println("[FTP] Error disconnecting: " + e.getMessage());
            }
        }
    }
    
    /**
     * Navega para diretório remoto, criando se necessário
     */
    private boolean changeToRemoteDirectory(FTPClient ftpClient, String remoteFolder) throws IOException {
        // Dividir o caminho em partes
        String[] folders = remoteFolder.split("/");
        
        for (String folder : folders) {
            if (folder.trim().isEmpty()) continue;
            
            // Tentar mudar para o diretório
            if (!ftpClient.changeWorkingDirectory(folder)) {
                // Se falhar, tentar criar
                if (!ftpClient.makeDirectory(folder)) {
                    System.out.println("[FTP] Failed to create directory: " + folder);
                    return false;
                }
                
                // Tentar mudar novamente
                if (!ftpClient.changeWorkingDirectory(folder)) {
                    System.out.println("[FTP] Failed to access directory: " + folder);
                    return false;
                }
            }
            System.out.println("[FTP] Directory: " + ftpClient.printWorkingDirectory());
        }
        
        return true;
    }
    
    /**
     * Resultado da operação FTP
     */
    public static class FtpResult {
        public final boolean success;
        public final String message;
        public final String remotePath;
        
        public FtpResult(boolean success, String message, String remotePath) {
            this.success = success;
            this.message = message;
            this.remotePath = remotePath;
        }
        
        @Override
        public String toString() {
            return success ? "✅ " + message : "❌ " + message;
        }
    }
}