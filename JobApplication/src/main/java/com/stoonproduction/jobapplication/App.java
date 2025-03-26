package com.stoonproduction.jobapplication;

import com.stoonproduction.jobapplication.config.DatabaseConfig;
import java.sql.Connection;

public class App {
    public static void main(String[] args) {
        System.out.println("***** Job Application Start *****");

        // Get the database connection
        Connection conn = DatabaseConfig.getConnection();

        if (conn != null) {
            System.out.println("✅ Database connection successful!");
        } else {
            System.out.println("❌ Database connection failed!");
        }

        DatabaseConfig.closeConnection();
    }
}
