package my_app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Teste para validar o fluxo correto de ADB que será implementado na UI
 * Baseado no aprendizado dos testes anteriores
 */
public class AdbCorrectFlowTest {
    
    public static void main(String[] args) {
        System.out.println("=== Correct ADB Flow Test ===");
        
        String ipPort = "192.168.3.104:35255";
        String pairCode = "581406";
        String filePath = "/home/eliezer/cat-categoria.mp4";
        String destFolder = "videos";
        
        // Fluxo correto que deve ser implementado na UI:
        
        // 1. Parear dispositivo
        boolean paired = pairDevice(ipPort, pairCode);
        
        // 2. Se pareou, tentar conectar
        boolean connected = false;
        if (paired) {
            connected = connectDevice(ipPort);
        }
        
        // 3. Verificar dispositivos novamente
        boolean deviceAvailable = checkDevices();
        
        // 4. Se tudo ok, tentar push
        if (deviceAvailable) {
            pushFile(filePath, destFolder);
        } else {
            System.out.println("❌ Cannot push - device not available");
        }
    }
    
    private static boolean pairDevice(String ipPort, String code) {
        System.out.println("\n--- Pairing ---");
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "pair", ipPort);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            if (code != null && !code.trim().isEmpty()) {
                process.getOutputStream().write((code + "\n").getBytes());
                process.getOutputStream().flush();
            }
            process.getOutputStream().close();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            System.out.println("Pairing output: " + output.toString());
            System.out.println("Exit code: " + exitCode);
            
            // Considerar sucesso mesmo com protocol fault se tiver indicação de sucesso
            String result = output.toString().toLowerCase();
            boolean success = result.contains("successfully paired") || 
                             result.contains("success") ||
                             (result.contains("protocol fault") && result.contains("success"));
            System.out.println("Pairing result: " + (success ? "✓ SUCCESS" : "✗ FAILED"));
            
            return success;
            
        } catch (Exception e) {
            System.out.println("Pairing error: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean connectDevice(String ipPort) {
        System.out.println("\n--- Connecting ---");
        try {
            // Tentar diferentes approaches
            String[] attempts = {
                ipPort,                    // Porta original
                ipPort.split(":")[0] + ":5555"  // Porta padrão ADB
            };
            
            for (String attempt : attempts) {
                System.out.println("Trying to connect to: " + attempt);
                
                ProcessBuilder pb = new ProcessBuilder("adb", "connect", attempt);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                int exitCode = process.waitFor();
                System.out.println("Connect output: " + output.toString());
                
                String outputStr = output.toString();
                if (outputStr.contains("connected to") && !outputStr.contains("failed")) {
                    System.out.println("✓ Connection successful to: " + attempt);
                    return true;
                } else if (outputStr.contains("failed") || outputStr.contains("Connection refused")) {
                    System.out.println("✗ Connection failed to: " + attempt);
                }
            }
            
            System.out.println("✗ All connection attempts failed");
            return false;
            
        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean checkDevices() {
        System.out.println("\n--- Checking Devices ---");
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "devices");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            boolean hasDevice = false;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("List of devices") && !line.trim().isEmpty()) {
                    hasDevice = true;
                    System.out.println("Found device: " + line);
                }
            }
            
            process.waitFor();
            System.out.println("Devices available: " + (hasDevice ? "✓ YES" : "✗ NO"));
            return hasDevice;
            
        } catch (Exception e) {
            System.out.println("Error checking devices: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean pushFile(String filePath, String destFolder) {
        System.out.println("\n--- Pushing File ---");
        try {
            String fullDest = "/storage/emulated/0/" + destFolder;
            ProcessBuilder pb = new ProcessBuilder("adb", "push", filePath, fullDest);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            System.out.println("Push output: " + output.toString());
            System.out.println("Exit code: " + exitCode);
            
            boolean success = exitCode == 0;
            System.out.println("Push result: " + (success ? "✓ SUCCESS" : "✗ FAILED"));
            return success;
            
        } catch (Exception e) {
            System.out.println("Push error: " + e.getMessage());
            return false;
        }
    }
}