package org.scd.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class sqlSetup {

    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static Connection conn;
    // Private constructor to prevent instantiation
    private sqlSetup() {}

    public static Connection getConnection() throws SQLException {
        if(conn == null){
            conn = DriverManager.getConnection(DB_URL);
        }
        return conn;
    }

}
