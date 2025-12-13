package dao;

import database.DatabaseConnection;
import factory.RoomFactory;
import model.room.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class RoomDAO {

    private final Connection connection;

    public RoomDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public int create(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, capacity, price_per_night, status) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getType());
            ps.setInt(3, room.getCapacity());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getStatus() == null ? "available" : room.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    room.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create room", e);
        }
        return -1;
    }

    public Room findById(int id) {
        String sql = "SELECT * FROM rooms WHERE room_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room", e);
        }
        return null;
    }

    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list rooms", e);
        }
        return rooms;
    }

    public List<Room> searchAllByTypeAndCapacity(String type, int capacity) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_type=? AND capacity>=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, capacity);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search rooms by type and capacity", e);
        }
        return rooms;
    }

    public List<Room> search(String type, int capacity, LocalDate startDate, LocalDate endDate) {
        List<Room> rooms = new ArrayList<>();
        String sql = """
                SELECT * FROM rooms r
                WHERE r.room_type=? AND r.capacity>=? AND r.status IN ('available','reserved')
                AND NOT EXISTS (
                    SELECT 1 FROM reservations res
                    WHERE res.room_id = r.room_id
                      AND res.status NOT IN ('canceled')
                      AND NOT (res.end_date <= ? OR res.start_date >= ?)
                )
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, capacity);
            ps.setDate(3, Date.valueOf(startDate));
            ps.setDate(4, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search rooms", e);
        }
        return rooms;
    }

    public void update(Room room) {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, capacity=?, price_per_night=?, status=? WHERE room_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getType());
            ps.setInt(3, room.getCapacity());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getStatus());
            ps.setInt(6, room.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room", e);
        }
    }

    public void updateStatus(int roomId, String status) {
        String sql = "UPDATE rooms SET status=? WHERE room_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room status", e);
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = RoomFactory.createRoom(rs.getString("room_type"));
        room.setId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setCapacity(rs.getInt("capacity"));
        room.setPricePerNight(rs.getDouble("price_per_night"));
        room.setStatus(rs.getString("status"));
        return room;
    }
}
