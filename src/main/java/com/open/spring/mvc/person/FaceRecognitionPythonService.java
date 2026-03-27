package com.open.spring.mvc.person;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FaceRecognitionPythonService {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionPythonService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Map<String, Object> identifyFace(String queryImageBase64, List<Map<String, String>> candidates, double threshold) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("image", queryImageBase64);
            payload.put("threshold", threshold);
            payload.put("candidates", candidates);

            String jsonInput = OBJECT_MAPPER.writeValueAsString(payload);

            File scriptFile = new File(System.getProperty("user.dir"), "scripts/deepface_match.py");
            if (!scriptFile.exists()) {
                scriptFile = new File("scripts/deepface_match.py");
            }
            String scriptPath = scriptFile.getAbsolutePath();

            String pythonPath = "python3";
            File venvPython = new File("../Pirna-flask/venv/bin/python3");
            if (venvPython.exists()) {
                pythonPath = venvPython.getAbsolutePath();
            }

            ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, scriptPath, "identify");
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try {
                process.getOutputStream().write(jsonInput.getBytes(StandardCharsets.UTF_8));
                process.getOutputStream().flush();
            } catch (IOException ioEx) {
                logger.error("Broken pipe while writing to DeepFace subprocess, output may be unavailable", ioEx);
                StringBuilder errorOutput = new StringBuilder();
                try (BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String errLine;
                    while ((errLine = errReader.readLine()) != null) {
                        errorOutput.append(errLine).append('\n');
                    }
                } catch (IOException innerErr) {
                    // best effort
                }
                return Map.of("match", false, "message", "DeepFace invocation error: Broken pipe", "detail", errorOutput.toString());
            } finally {
                try {
                    process.getOutputStream().close();
                } catch (IOException ignored) {
                }
            }

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("DeepFace python script exited with code {} output={}", exitCode, output.toString());
                return Map.of("match", false, "message", "DeepFace subprocess failed", "detail", output.toString());
            }

            try {
                return OBJECT_MAPPER.readValue(output.toString(), Map.class);
            } catch (Exception ex) {
                logger.error("Failed to parse DeepFace JSON output: {}", output.toString(), ex);
                return Map.of("match", false, "message", "Invalid DeepFace output");
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to run DeepFace python script", e);
            Thread.currentThread().interrupt();
            return Map.of("match", false, "message", "DeepFace invocation error: " + e.getMessage());
        }
    }
}
