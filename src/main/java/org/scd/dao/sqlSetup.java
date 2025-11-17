package org.scd.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class sqlSetup {

    private static final String DB_URL = "jdbc:sqlite:database.db";

    // Private constructor to prevent instantiation
    private sqlSetup() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("SQLite database connected successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = getConnection();
        String q = """
    
    """;
        Statement stml = conn.createStatement();
        stml.executeQuery(q);
    }
}
