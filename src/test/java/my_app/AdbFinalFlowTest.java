package my_app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Teste final para validar o fluxo ADB que será implementado na UI
 * Considerando as limitações descobertas
 */
public class AdbFinalFlowTest {
    
    public static void main(String[] args) {
        System.out.println("=== Final ADB Flow Test ===");
        System.out.println("This test validates the correct flow for UI implementation\n");
        
        testCorrectAdbFlow();
    }
    
    private static void testCorrectAdbFlow() {
        String ipPort = "192.168.3.104:35255";
        String pairCode = "581406";
        
        System.out.println("=== CORRECT FLOW FOR UI IMPLEMENTATION ===\n");
        
        // PASSO 1: Parear (considera sucesso mesmo com protocol fault)
        boolean pairSuccess = pairDeviceWithProtocolFault(ipPort, pairCode);
        System.out.println("Pair result: " + (pairSuccess ? "✓" : "✗"));
        
        // PASSO 2: Tentar conectar em diferentes portas
        boolean connectSuccess = tryMultipleConnections(ipPort);
        System.out.println("Connect result: " + (connectSuccess ? "✓" : "✗"));
        
        // PASSO 3: Verificar dispositivo
        boolean deviceAvailable = checkDeviceAvailability();
        System.out.println("Device available: " + (deviceAvailable ? "✓" : "✗"));
        
        System.out.println("\n=== RECOMMENDATIONS FOR UI ===");
        if (!pairSuccess) {
            System.out.println("❌ Pairing failed - check code and device");
        } else if (!connectSuccess) {
            System.out.println("⚠️  Pairing OK but connection failed");
            System.out.println("   User must enable 'Wireless Debugging' on device");
            System.out.println("   Settings -> Developer Options -> Wireless Debugging");
        } else if (!deviceAvailable) {
            System.out.println("⚠️  Connected but device not showing");
            System.out.println("   Try restarting ADB server: adb kill-server && adb start-server");
        } else {
            System.out.println("✅ All steps passed - ready for file push!");
        }
    }
    
    private static boolean pairDeviceWithProtocolFault(String ipPort, String code) {
        System.out.println("1. Pairing device...");
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
            
            process.waitFor();
            String result = output.toString().toLowerCase();
            
            // Sucesso mesmo com protocol fault
            boolean success = result.contains("successfully paired") || 
                             (result.contains("protocol fault") && result.contains("success"));
            
            System.out.println("   Output: " + output.toString().trim());
            return success;
            
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean tryMultipleConnections(String originalIpPort) {
        System.out.println("2. Attempting connections...");
        
        String[] attempts = {
            originalIpPort,
            originalIpPort.split(":")[0] + ":5555"
        };
        
        for (String attempt : attempts) {
            try {
                System.out.println("   Trying: " + attempt);
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
                
                process.waitFor();
                String result = output.toString();
                System.out.println("   Result: " + result.trim());
                
                if (result.contains("connected to") && !result.contains("failed")) {
                    System.out.println("   ✓ Connected successfully!");
                    return true;
                }
                
            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    private static boolean checkDeviceAvailability() {
        System.out.println("3. Checking device availability...");
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
                    System.out.println("   Found: " + line);
                }
            }
            
            process.waitFor();
            return hasDevice;
            
        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
            return false;
        }
    }
}