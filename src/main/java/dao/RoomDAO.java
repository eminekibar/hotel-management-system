package dao;

import database.DatabaseConnection;
import factory.RoomFactory;
import model.room.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            ps.setString(5, "available");
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

    public List<Room> search(String type, int capacity) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_type=? AND capacity>=? AND status='available'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, capacity);
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
        String sql = "UPDATE rooms SET room_number=?, capacity=?, price_per_night=?, status=? WHERE room_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getCapacity());
            ps.setDouble(3, room.getPricePerNight());
            ps.setString(4, "available");
            ps.setInt(5, room.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room", e);
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = RoomFactory.createRoom(rs.getString("room_type"));
        room.setId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setCapacity(rs.getInt("capacity"));
        room.setPricePerNight(rs.getDouble("price_per_night"));
        return room;
    }
}
