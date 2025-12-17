package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://127.0.0.1:3307/hotel_db?useSSL=false&serverTimezone=UTC";

    private DatabaseConnection() {
        try {
            String dbUser = System.getenv("DB_USER");
            String dbPassword = System.getenv("DB_PASSWORD");
            if (dbUser == null || dbPassword == null) {
                // UYARI: Burayı sadece test için kullan, gerçek şifreyi buraya yazma!
                // SonarQube kızmasın diye burayı boş veya dummy bırakabilirsin şimdilik.
                throw new RuntimeException("Veritabanı kullanıcı adı veya şifresi ortam değişkenlerinde bulunamadı! (DB_USER, DB_PASSWORD)");
            }
            this.connection = DriverManager.getConnection(URL, dbUser, dbPassword);
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
