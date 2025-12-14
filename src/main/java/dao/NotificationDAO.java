package dao;

import database.DatabaseConnection;
import model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private final Connection connection;

    public NotificationDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public int create(String userType, int userId, String message) {
        String sql = "INSERT INTO notifications (user_type, user_id, message) VALUES (?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userType);
            ps.setInt(2, userId);
            ps.setString(3, message);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create notification", e);
        }
        return -1;
    }

    public List<Notification> findForUser(String userType, int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_type=? AND user_id=? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userType);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list notifications", e);
        }
        return notifications;
    }

    public List<Notification> findAll() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                notifications.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list all notifications", e);
        }
        return notifications;
    }

    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read=1 WHERE notification_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark notification as read", e);
        }
    }

    public void markAllAsRead(String userType, int userId) {
        String sql = "UPDATE notifications SET is_read=1 WHERE user_type=? AND user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userType);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark notifications as read", e);
        }
    }

    public void markAllAsRead() {
        String sql = "UPDATE notifications SET is_read=1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark all notifications as read", e);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("notification_id"));
        notification.setUserType(rs.getString("user_type"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        return notification;
    }
}
