package my_app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Teste estendido para diagnóstico completo de ADB
 */
public class AdbDiagnosisTest {
    
    private static String DEVICE_IP = "192.168.3.104:35255";
    private static String PAIR_CODE = "581406";
    
    public static void main(String[] args) {
        System.out.println("=== ADB Diagnosis Test ===");
        
        // 1. Verificar versão e status do ADB
        testAdbVersion();
        
        // 2. Listar dispositivos
        testFindDevices();
        
        // 3. Tentar pareamento novamente
        testPairDevice();
        
        // 4. Tentar descobrir dispositivos na rede
        testDiscoverDevices();
        
        // 5. Tentar diferentes formas de conexão
        testDifferentConnections();
        
        // 6. Verificar status final
        testFindDevices();
    }
    
    private static void testAdbVersion() {
        System.out.println("\n--- ADB Version & Status ---");
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            System.out.println("Version info:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            process.waitFor();
            
            // Verificar se o servidor ADB está rodando
            ProcessBuilder serverPb = new ProcessBuilder("adb", "start-server");
            serverPb.redirectErrorStream(true);
            Process serverProcess = serverPb.start();
            serverProcess.waitFor();
            System.out.println("ADB server started/restarted");
            
        } catch (Exception e) {
            System.out.println("Error checking ADB: " + e.getMessage());
        }
    }
    
    private static void testFindDevices() {
        System.out.println("\n--- Find Devices ---");
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "devices", "-l");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            System.out.println("Devices list:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void testPairDevice() {
        System.out.println("\n--- Pair Device ---");
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "pair", DEVICE_IP);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Enviar código
            process.getOutputStream().write((PAIR_CODE + "\n").getBytes());
            process.getOutputStream().flush();
            process.getOutputStream().close();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            System.out.println("Pairing output:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void testDiscoverDevices() {
        System.out.println("\n--- Discover Devices ---");
        try {
            // Tentar descobrir dispositivos via mDNS
            ProcessBuilder pb = new ProcessBuilder("adb", "mdns", "services");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            System.out.println("mDNS services:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
            
        } catch (Exception e) {
            System.out.println("Error discovering devices: " + e.getMessage());
        }
    }
    
    private static void testDifferentConnections() {
        System.out.println("\n--- Different Connection Attempts ---");
        
        // Tentar conectar sem porta
        tryConnect("192.168.3.104", "Without port");
        
        // Tentar conectar com porta diferente (5037 é padrão ADB)
        tryConnect("192.168.3.104:5037", "With port 5037");
        
        // Tentar com a porta original
        tryConnect(DEVICE_IP, "With original port");
        
        // Tentar IPCONNECT
        try {
            System.out.println("\nTrying adb command with IP...");
            ProcessBuilder pb = new ProcessBuilder("adb", "-P", "5037", "connect", DEVICE_IP);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            System.out.println("adb -P connect output:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            process.waitFor();
            
        } catch (Exception e) {
            System.out.println("Error with adb -P connect: " + e.getMessage());
        }
    }
    
    private static void tryConnect(String target, String description) {
        System.out.println("\n" + description + ": " + target);
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "connect", target);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}