package com.stoonproduction.jobapplication;

import static org.junit.jupiter.api.Assertions.*;

import com.stoonproduction.jobapplication.config.DatabaseConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

class DatabaseConfigTest {

    private static Connection connection;

    @BeforeAll
    static void setUp() {
        connection = DatabaseConfig.getConnection();
    }

    @Test
    void testDatabaseConnection() {
        assertNotNull(connection, "Connection should not be null");
        try {
            assertFalse(connection.isClosed(), "Connection should be open");
            System.out.println("✅ Database connection is active!");
        } catch (SQLException e) {
            fail("❌ Failed to check connection status: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        // Close connection after tests
        DatabaseConfig.closeConnection();
        try {
            assertTrue(connection.isClosed(), "Connection should be closed after test");
            System.out.println("✅ Database connection successfully closed!");
        } catch (SQLException e) {
            fail("❌ Failed to close connection: " + e.getMessage());
        }
    }
}
