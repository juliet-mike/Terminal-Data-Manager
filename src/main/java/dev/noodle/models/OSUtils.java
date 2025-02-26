package dev.noodle.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OSUtils {
    public static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "Windows";
        if (os.contains("mac")) return "macOS";
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return "Linux";
        return "Unknown";
    }

    public static void main(String[] args) {
        String os = getOS();
        System.out.println("Detected OS: " + os);

        String command = switch (os) {
            case "Windows" -> "wsl neofetch";  // If WSL is available
            case "Linux", "macOS" -> "neofetch";
            default -> throw new RuntimeException("Unsupported OS");
        };

        try {
            ProcessBuilder pb = new ProcessBuilder(command.split(" "));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
