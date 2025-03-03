package com.nighthawk.spring_portfolio.mvc.backups;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/exports/")
public class BackupsController {

    // Hardcoded database path
    private static final String DB_PATH = "./volumes/sqlite.db";

    // Hardcoded JSON file directory
    private static final String BACKUP_DIR = "./volumes/backups/";

    // ObjectMapper for JSON serialization
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Endpoint to retrieve data from all tables in the database
    @GetMapping("/getAll")
    public void getAllTablesData(HttpServletResponse response) {
        // Export data from the database
        Map<String, List<Map<String, Object>>> data = exportData();

        // Set response headers for file download
        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=exports.json");

        // Write the JSON data to the response output stream
        try (OutputStream out = response.getOutputStream()) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to stream JSON data to the client", e);
        }
    }

    // Method to export data from the database
    private Map<String, List<Map<String, Object>>> exportData() {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             Statement statement = connection.createStatement()) {

            // Get the list of tables in the database
            List<String> tableNames = getTableNames(connection);

            // Loop through each table and retrieve its data
            for (String tableName : tableNames) {
                List<Map<String, Object>> tableData = getTableData(statement, tableName);
                result.put(tableName, tableData);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve data from the database", e);
        }

        return result;
    }

    // Helper method to get the list of table names in the database
    private List<String> getTableNames(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();

        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
        }

        return tableNames;
    }

    // Helper method to retrieve data from a specific table
    private List<Map<String, Object>> getTableData(Statement statement, String tableName) throws SQLException {
        List<Map<String, Object>> tableData = new ArrayList<>();

        try (ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName)) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);

                    // Handle special fields (e.g., stats, kasm_server_needed)
                    if (columnName.equals("stats") && columnValue instanceof String) {
                        // If stats is stored as a string, parse it as JSON
                        columnValue = objectMapper.readValue((String) columnValue, Map.class);
                    } else if (columnName.equals("kasm_server_needed") && columnValue instanceof Integer) {
                        // If kasm_server_needed is stored as an integer, convert it to boolean
                        columnValue = ((Integer) columnValue) == 1;
                    }

                    row.put(columnName, columnValue);
                }
                tableData.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Failed to retrieve data from table: " + tableName, e);
        }

        return tableData;
    }

    // This method will be called just before the server stops
    @jakarta.annotation.PreDestroy
    public void onShutdown() {
        System.out.println("Server is stopping. Exporting data...");

        // Export data
        Map<String, List<Map<String, Object>>> data = exportData();

        // Save the data to a JSON file with a timestamp
        saveJsonToFile(data);

        // Manage backups to keep only the three most recent ones
        manageBackups();

        System.out.println("Data export completed.");
    }

    // Helper method to save the JSON data to a file with a timestamp
    private void saveJsonToFile(Map<String, List<Map<String, Object>>> data) {
        try {
            // Create the backups directory if it doesn't exist
            File backupsDir = new File(BACKUP_DIR);
            if (!backupsDir.exists()) {
                backupsDir.mkdirs();
            }

            // Generate a timestamp for the filename
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String fileName = "backup_" + timeStamp + ".json";
            File jsonFile = new File(backupsDir, fileName);

            // Write the JSON data to the file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, data);
            System.out.println("JSON data saved to: " + jsonFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save JSON data to file", e);
        }
    }

    // Helper method to manage backups, keeping only the three most recent ones
    private void manageBackups() {
        File backupsDir = new File(BACKUP_DIR);
        if (!backupsDir.exists() || !backupsDir.isDirectory()) {
            return;
        }

        // Get all backup files
        File[] backupFiles = backupsDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".json"));

        if (backupFiles == null || backupFiles.length <= 3) {
            return;
        }

        // Sort files by last modified date (oldest first)
        Arrays.sort(backupFiles, Comparator.comparingLong(File::lastModified));

        // Delete the oldest files if there are more than three
        for (int i = 0; i < backupFiles.length - 3; i++) {
            if (backupFiles[i].delete()) {
                System.out.println("Deleted old backup: " + backupFiles[i].getName());
            } else {
                System.out.println("Failed to delete old backup: " + backupFiles[i].getName());
            }
        }
    }
}