package com.example.smartbuy.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
* Database connection utility class
* Manages MySQL database connections
 */
public class DatabaseUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/smartbuy_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "lbx050711";

    // Private constructor to prevent instantiation
    private DatabaseUtil() {}

    /**
     * Get database connection
     * @return Connection object
     * @throws SQLException if the connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL driver not found.", e);
        }
    }

    /**
     * Close the database connection
     * @param connection The connection to be closed
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
   * Test database connection
   * @return Whether the connection is successful
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}
