package com.open.spring.system;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Helper class to detect and work with different database types (SQLite vs MySQL).
 * Provides utilities to determine which database is being used and handle
 * database-specific SQL syntax differences.
 */
@Component
public class DatabaseConfig {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private Environment environment;
    
    /**
     * Detects if the current database is MySQL.
     * Checks the datasource URL or database product name.
     * 
     * @return true if using MySQL, false if using SQLite or other
     */
    public boolean isMySQL() {
        try {
            // Check environment variable first
            String dbType = environment.getProperty("DB_TYPE");
            if ("mysql".equalsIgnoreCase(dbType)) {
                return true;
            }
            
            // Check datasource URL
            String url = environment.getProperty("spring.datasource.url", "");
            if (url.contains("mysql") || url.contains("jdbc:mysql")) {
                return true;
            }
            
            // Check database metadata as fallback
            if (dataSource != null) {
                try (Connection conn = dataSource.getConnection()) {
                    DatabaseMetaData metaData = conn.getMetaData();
                    String productName = metaData.getDatabaseProductName();
                    if (productName != null && productName.toLowerCase().contains("mysql")) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            // If we can't determine, default to SQLite
            System.err.println("Warning: Could not determine database type: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Detects if the current database is SQLite.
     * 
     * @return true if using SQLite, false otherwise
     */
    public boolean isSQLite() {
        return !isMySQL();
    }
}
