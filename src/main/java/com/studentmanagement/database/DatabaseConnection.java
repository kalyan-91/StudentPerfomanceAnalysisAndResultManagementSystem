package com.studentmanagement.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Manager
 * Member 6: D.PAVAN KALYAN - Database & JDBC Layer
 * Local MySQL - Fast
 */
public class DatabaseConnection {

    private static final String DB_URL      = "jdbc:mysql://localhost:3306/student_management_db"
                                            + "?useSSL=false"
                                            + "&allowPublicKeyRetrieval=true"
                                            + "&serverTimezone=UTC"
                                            + "&autoReconnect=true"
                                            + "&cachePrepStmts=true"
                                            + "&useServerPrepStmts=true"
                                            + "&rewriteBatchedStatements=true"
                                            + "&cacheServerConfiguration=true"
                                            + "&elideSetAutoCommits=true"
                                            + "&maintainTimeStats=false";

    private static final String DB_USER     = "student";
    private static final String DB_PASSWORD = "student123";
    private static final String DB_DRIVER   = "com.mysql.cj.jdbc.Driver";

    private static Connection connection = null;

    private DatabaseConnection() {}

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DB_DRIVER);
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("DATABASE CONNECTION ESTABLISHED SUCCESSFULLY!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to local database!");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}