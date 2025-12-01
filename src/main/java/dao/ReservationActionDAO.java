package dao;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReservationActionDAO {
    private final Connection connection;

    public ReservationActionDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void logCheckIn(int reservationId, int staffId) {
        log(reservationId, staffId, "check_in");
    }

    public void logCheckOut(int reservationId, int staffId) {
        log(reservationId, staffId, "check_out");
    }

    public void logCancel(int reservationId, int staffId) {
        log(reservationId, staffId, "cancel");
    }

    private void log(int reservationId, int staffId, String type) {
        String sql = "INSERT INTO reservation_actions (reservation_id, staff_id, action_type) VALUES (?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, staffId);
            ps.setString(3, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to log reservation action", e);
        }
    }
}
