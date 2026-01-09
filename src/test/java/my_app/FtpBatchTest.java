package my_app;

/**
 * Teste para upload em lote via FTP
 */
public class FtpBatchTest {
    
    public static void main(String[] args) {
        System.out.println("=== FTP Batch Upload Test ===");
        
        // Configuração FTP
        String server = "192.168.3.104";
        int port = 2221;
        String username = "android";
        String password = "android";
        
        FtpService ftpService = new FtpService(server, port, username, password);
        
        // Lista de arquivos para teste
        String[] testFiles = {
            "/home/eliezer/2025-11-12 19-41-56.mp4",
            "/home/eliezer/cat1.mp4"
        };
        
        System.out.println("Files to upload:");
        for (int i = 0; i < testFiles.length; i++) {
            System.out.println((i + 1) + ". " + testFiles[i]);
        }
        
        System.out.println("\nStarting batch upload...");
        
        int successCount = 0;
        for (int i = 0; i < testFiles.length; i++) {
            String filePath = testFiles[i];
            String fileName = new java.io.File(filePath).getName();
            
            System.out.println("\nUploading " + (i + 1) + "/" + testFiles.length + ": " + fileName);
            
            FtpService.FtpResult result = ftpService.uploadFile(filePath, "batch_test");
            
            System.out.println("Result: " + result.toString());
            
            if (result.success) {
                successCount++;
                System.out.println("✓ Success");
            } else {
                System.out.println("✗ Failed");
            }
        }
        
        System.out.println("\n=== Batch Upload Summary ===");
        System.out.println("Total files: " + testFiles.length);
        System.out.println("Successful: " + successCount);
        System.out.println("Failed: " + (testFiles.length - successCount));
        System.out.println("Success rate: " + (successCount * 100 / testFiles.length) + "%");
    }
}