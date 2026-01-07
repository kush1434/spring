package com.open.spring.mvc.javarunner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/run")
@CrossOrigin(origins = {"http://127.0.0.1:4500","https://pages.opencodingsociety.com"}, allowCredentials = "true")
public class JavaRunnerApiController {

    private static final int MAX_OUTPUT_SIZE = 10000; // Maximum characters in output
    private static final int TIMEOUT_SECONDS = 5;
    private static final String MEMORY_LIMIT = "128m"; // 128MB RAM limit
    private static final String CPU_LIMIT = "0.5"; // 0.5 CPU cores

    @PostMapping("/java")
    public ResponseEntity<Map<String, String>> runJava(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.trim().isEmpty()) {
            return new ResponseEntity<>(Map.of("output", "No code provided."), HttpStatus.BAD_REQUEST);
        }

        // Validate code length to prevent abuse
        if (code.length() > 50000) {
            return new ResponseEntity<>(Map.of("output", "Code too long (max 50KB)."), HttpStatus.BAD_REQUEST);
        }

        String containerId = null;
        Path tempDir = null;

        try {
            // Create temporary directory for this execution
            tempDir = Files.createTempDirectory("java-sandbox-");
            Path javaFile = tempDir.resolve("Main.java");
            Files.writeString(javaFile, code);

            // Generate unique container name
            containerId = "java-runner-" + UUID.randomUUID().toString().substring(0, 8);

            // Build the Docker command with security restrictions
            List<String> dockerCommand = Arrays.asList(
                "docker", "run",
                "--name", containerId,
                "--rm", // Auto-remove container after execution
                "--network", "none", // No network access
                "--memory", MEMORY_LIMIT, // Memory limit
                "--cpus", CPU_LIMIT, // CPU limit
                "--pids-limit", "50", // Limit number of processes
                "--read-only", // Read-only root filesystem
                "--tmpfs", "/tmp:rw,noexec,nosuid,size=10m", // Small writable tmp
                "--security-opt", "no-new-privileges", // Prevent privilege escalation
                "--cap-drop", "ALL", // Drop all capabilities
                "-v", tempDir.toAbsolutePath() + ":/workspace:ro", // Mount code as read-only
                "-w", "/workspace",
                "openjdk:21-slim", // Use official OpenJDK image
                "sh", "-c",
                // Compile and run in one command inside container
                "javac Main.java && timeout " + TIMEOUT_SECONDS + " java Main"
            );

            // Execute in Docker container
            ProcessBuilder pb = new ProcessBuilder(dockerCommand);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output with size limit
            String output = readOutput(process.getInputStream(), MAX_OUTPUT_SIZE);
            
            // Wait for completion with timeout
            boolean finished = process.waitFor(TIMEOUT_SECONDS + 2, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                // Try to force stop the container
                forceStopContainer(containerId);
                cleanup(tempDir);
                return new ResponseEntity<>(Map.of("output", "Execution timed out (" + TIMEOUT_SECONDS + "s limit)."), HttpStatus.OK);
            }

            int exitCode = process.exitValue();
            
            // Format output based on exit code
            String finalOutput;
            if (exitCode == 0) {
                finalOutput = output.isEmpty() ? "Code executed successfully (no output)" : output;
            } else if (output.contains("error:")) {
                finalOutput = "Compilation error:\n" + output;
            } else if (exitCode == 124 || exitCode == 137) {
                finalOutput = "Execution timed out (" + TIMEOUT_SECONDS + "s limit).";
            } else {
                finalOutput = "Runtime error:\n" + output;
            }

            cleanup(tempDir);
            return new ResponseEntity<>(Map.of("output", finalOutput), HttpStatus.OK);

        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cannot run program \"docker\"")) {
                cleanup(tempDir);
                return new ResponseEntity<>(
                    Map.of("output", "Docker is not available. Please ensure Docker is installed and running."), 
                    HttpStatus.SERVICE_UNAVAILABLE
                );
            }
            cleanup(tempDir);
            return new ResponseEntity<>(
                Map.of("output", "Error: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            cleanup(tempDir);
            if (containerId != null) {
                forceStopContainer(containerId);
            }
            return new ResponseEntity<>(
                Map.of("output", "Unexpected error: " + e.getMessage()), 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Read process output with size limit to prevent memory exhaustion
     */
    private String readOutput(InputStream inputStream, int maxSize) throws IOException {
        StringBuilder output = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        int totalRead = 0;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            totalRead += bytesRead;
            if (totalRead > maxSize) {
                output.append(new String(buffer, 0, bytesRead, "UTF-8"));
                output.append("\n... (output truncated, exceeded ").append(maxSize).append(" characters)");
                break;
            }
            output.append(new String(buffer, 0, bytesRead, "UTF-8"));
        }

        return output.toString();
    }

    /**
     * Force stop and remove a Docker container
     */
    private void forceStopContainer(String containerId) {
        try {
            new ProcessBuilder("docker", "rm", "-f", containerId)
                .start()
                .waitFor(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // Best effort cleanup
        }
    }

    /**
     * Cleanup temporary files
     */
    private void cleanup(Path dir) {
        if (dir == null) return;
        try {
            Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try { Files.delete(path); } catch (IOException ignored) {}
                });
        } catch (IOException ignored) {}
    }
}