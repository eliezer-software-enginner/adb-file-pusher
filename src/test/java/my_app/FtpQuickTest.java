package my_app;

/**
 * Teste rápido para validar a implementação FTP
 */
public class FtpQuickTest {
    
    public static void main(String[] args) {
        System.out.println("=== FTP Quick Test ===");
        
        // Configuração do seu servidor FTP
        String server = "192.168.3.104";
        int port = 2221;
        String username = "android";
        String password = "android";
        
        FtpService ftpService = new FtpService(server, port, username, password);
        
        // Testar conexão
        System.out.println("1. Testing connection...");
        boolean connected = ftpService.testConnection();
        System.out.println("Connection result: " + (connected ? "✅ SUCCESS" : "❌ FAILED"));
        
        if (connected) {
            // Testar upload
            System.out.println("\n2. Testing upload...");
            String testFile = "/home/eliezer/2025-11-12 19-41-56.mp4"; // Arquivo menor
            String destFolder = "test";
            
            FtpService.FtpResult result = ftpService.uploadFile(testFile, destFolder);
            System.out.println("Upload result: " + result.toString());
            
            if (result.success) {
                System.out.println("Remote path: " + result.remotePath);
            }
        } else {
            System.out.println("❌ Cannot test upload - connection failed");
        }
    }
}