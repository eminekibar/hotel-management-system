package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection holder.
 */
public final class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // Use explicit IPv4 to avoid IPv6/localhost binding issues on MariaDB
    private static final String URL = "jdbc:mysql://127.0.0.1:3307/hotel_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database connection", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
