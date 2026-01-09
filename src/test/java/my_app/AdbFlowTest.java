package my_app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Teste isolado para validar o fluxo completo de operações ADB
 * - Pareamento
 * - Conexão 
 * - Push
 */
public class AdbFlowTest {
    
    private static String DEVICE_IP = "192.168.3.104:35255";
    private static String PAIR_CODE = "581406";
    
    public static void main(String[] args) {
        System.out.println("=== ADB Flow Test ===");
        
        // Teste 1: Verificar dispositivos conectados
        testFindDevices();
        
        // Teste 2: Parear dispositivo (se necessário)
        testPairDevice(DEVICE_IP, PAIR_CODE);
        
        // Teste 3: Conectar ao dispositivo via TCP (mesmo com erro de pair)
        testConnectDevice();
        
        // Teste 4: Aguardar e verificar dispositivos novamente
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        testFindDevices();
        
        // Teste 5: Tentar push
        testPush("/home/eliezer/cat-categoria.mp4", "videos");
    }
    
    private static void testConnectDevice() {
        System.out.println("\n--- Test: Connect Device ---");
        System.out.println("Target: " + DEVICE_IP);
        
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "connect", DEVICE_IP);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            System.out.println("Output:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testFindDevices() {
        System.out.println("\n--- Test: Find Devices ---");
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "devices");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            boolean hasDevice = false;
            System.out.println("Output:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
                if (!line.startsWith("List of devices") && !line.trim().isEmpty()) {
                    hasDevice = true;
                }
            }
            
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
            System.out.println("Has devices: " + hasDevice);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testPairDevice(String ipPort, String code) {
        System.out.println("\n--- Test: Pair Device ---");
        System.out.println("Target: " + ipPort);
        System.out.println("Code: " + code);
        
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "pair", ipPort);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Enviar código
            if (code != null && !code.trim().isEmpty()) {
                process.getOutputStream().write((code + "\n").getBytes());
                process.getOutputStream().flush();
            }
            process.getOutputStream().close();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            System.out.println("Output:");
            while ((line = reader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testPush(String filePath, String destFolder) {
        System.out.println("\n--- Test: Push File ---");
        System.out.println("File: " + filePath);
        System.out.println("Destination: " + destFolder);
        
        try {
            // Primeiro verificar se o dispositivo está conectado
            ProcessBuilder checkPb = new ProcessBuilder("adb", "devices");
            checkPb.redirectErrorStream(true);
            Process checkProcess = checkPb.start();
            
            BufferedReader checkReader = new BufferedReader(
                new InputStreamReader(checkProcess.getInputStream())
            );
            
            boolean deviceConnected = false;
            String line;
            while ((line = checkReader.readLine()) != null) {
                if (!line.startsWith("List of devices") && !line.trim().isEmpty()) {
                    deviceConnected = true;
                    break;
                }
            }
            
            checkProcess.waitFor();
            
            if (!deviceConnected) {
                System.out.println("❌ No device connected - cannot push");
                return;
            }
            
            System.out.println("✓ Device connected - proceeding with push");
            
            // Tentar conectar via TCP se necessário
            ProcessBuilder connectPb = new ProcessBuilder("adb", "connect", DEVICE_IP);
            connectPb.redirectErrorStream(true);
            Process connectProcess = connectPb.start();
            
            BufferedReader connectReader = new BufferedReader(
                new InputStreamReader(connectProcess.getInputStream())
            );
            
            System.out.println("Connect output:");
            while ((line = connectReader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            connectProcess.waitFor();
            
            // Agora tentar o push
            String fullDest = "/storage/emulated/0/" + destFolder;
            ProcessBuilder pushPb = new ProcessBuilder("adb", "push", filePath, fullDest);
            pushPb.redirectErrorStream(true);
            Process pushProcess = pushPb.start();
            
            BufferedReader pushReader = new BufferedReader(
                new InputStreamReader(pushProcess.getInputStream())
            );
            
            System.out.println("Push output:");
            while ((line = pushReader.readLine()) != null) {
                System.out.println("  " + line);
            }
            
            int exitCode = pushProcess.waitFor();
            System.out.println("Push exit code: " + exitCode);
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}