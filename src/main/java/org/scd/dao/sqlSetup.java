package org.scd.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class sqlSetup {

    //private static final String DB_URL = "jdbc:sqlite:database.db";
    private static final String DB_URL = "jdbc:sqlite::memory:";

    private static Connection conn;

    private sqlSetup() {}

    public static Connection getConnection() throws SQLException {
        if (conn == null) {
            System.out.println("[sqlSetup] Opening SQLite connection…");
            conn = DriverManager.getConnection(DB_URL);

            System.out.println("[sqlSetup] Connected to DB: " + conn.getMetaData().getURL());
            System.out.println("[sqlSetup] DB File Absolute Path: " +
                    new java.io.File("database.db").getAbsolutePath());

            try (Statement stmt = conn.createStatement()) {
                System.out.println("[sqlSetup] Enabling foreign keys…");
                stmt.execute("PRAGMA foreign_keys = ON");
                System.out.println("[sqlSetup] Foreign key PRAGMA OK.");
            }

            // Healthy ping query
            try (Statement stmt = conn.createStatement()) {
                System.out.println("[sqlSetup] Running ping test SELECT 1…");
                stmt.execute("SELECT 1");
                System.out.println("[sqlSetup] Ping OK.");
            }
        } else {
            System.out.println("[sqlSetup] Reusing existing DB connection.");
        }

        return conn;
    }
}
