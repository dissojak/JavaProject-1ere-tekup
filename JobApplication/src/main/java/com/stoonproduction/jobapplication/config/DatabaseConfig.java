// src/main/java/config/DatabaseConfig.java
package com.stoonproduction.jobapplication.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 3306;
    private static final String DB_NAME = "JobApp";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    public static Connection getConnection() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create connection if it doesn't exist or is closed
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(
                        String.format("jdbc:mysql://%s:%d/%s", HOST, PORT, DB_NAME),
                        USERNAME,
                        PASSWORD
                );
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found");
            e.printStackTrace();
        } catch (SQLException se) {
            System.err.println("Database connection failed");
            se.printStackTrace();
        }
        return connection;
    }

    // Optional: Method to close connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection successfully closed! #MAIN");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}