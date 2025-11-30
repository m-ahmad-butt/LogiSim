package org.scd.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class sqlSetup {

    private static Connection conn;

    private sqlSetup() {}

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                java.util.Properties props = new java.util.Properties();
                try (java.io.InputStream is = sqlSetup.class.getClassLoader().getResourceAsStream("system.properties")) {
                    if (is != null) {
                        props.load(is);
                    } else {
                        System.err.println("[sqlSetup] system.properties not found!");
                    }
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

                String dbUrl = props.getProperty("db.url", "jdbc:sqlite:database.db");
                System.out.println("[sqlSetup] Opening SQLite connection to: " + dbUrl);
                
                conn = DriverManager.getConnection(dbUrl);

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
                
                createTables(conn);

            } catch (Exception e) {
                throw new SQLException("Failed to connect to database", e);
            }
        }
        return conn;
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Project Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Project (" +
                    "projectID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "projectName TEXT NOT NULL)");

            // Circuit Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Circuit (" +
                    "circuitID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "projectID INTEGER, " +
                    "circuitName TEXT, " +
                    "FOREIGN KEY(projectID) REFERENCES Project(projectID) ON DELETE CASCADE)");

            // Gate Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Gate (" +
                    "component_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "circuit_id INTEGER, " +
                    "component_type TEXT, " +
                    "positionX REAL, " +
                    "positionY REAL, " +
                    "component_output INTEGER, " +
                    "FOREIGN KEY(circuit_id) REFERENCES Circuit(circuitID) ON DELETE CASCADE)");

            // Gate_Input Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Gate_Input (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "component_id INTEGER, " +
                    "input_value TEXT, " +
                    "input_order TEXT, " +
                    "FOREIGN KEY(component_id) REFERENCES Gate(component_id) ON DELETE CASCADE)");

            // Connector Table
            stmt.execute("CREATE TABLE IF NOT EXISTS Connector (" +
                    "connector_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "component_color TEXT, " +
                    "source_id INTEGER, " +
                    "sink_id INTEGER, " +
                    "FOREIGN KEY(source_id) REFERENCES Gate(component_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(sink_id) REFERENCES Gate(component_id) ON DELETE CASCADE)");
            
            System.out.println("[sqlSetup] Tables checked/created.");
        }
    }
}
