package dao;

import database.DatabaseConnection;
import model.user.Staff;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    private final Connection connection;

    public StaffDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public int create(Staff staff) {
        String sql = "INSERT INTO staff (first_name, last_name, email, password_hash, role) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, staff.getFirstName());
            ps.setString(2, staff.getLastName());
            ps.setString(3, staff.getEmail());
            ps.setString(4, staff.getPasswordHash());
            ps.setString(5, staff.getRole());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    staff.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create staff", e);
        }
        return -1;
    }

    public Staff findById(int id) {
        String sql = "SELECT * FROM staff WHERE staff_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find staff", e);
        }
        return null;
    }

    public Staff findByEmail(String email) {
        String sql = "SELECT * FROM staff WHERE email=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find staff by email", e);
        }
        return null;
    }

    public List<Staff> findAll() {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM staff";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                staffList.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list staff", e);
        }
        return staffList;
    }

    public void update(Staff staff) {
        String sql = "UPDATE staff SET first_name=?, last_name=?, email=?, password_hash=?, role=? WHERE staff_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, staff.getFirstName());
            ps.setString(2, staff.getLastName());
            ps.setString(3, staff.getEmail());
            ps.setString(4, staff.getPasswordHash());
            ps.setString(5, staff.getRole());
            ps.setInt(6, staff.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update staff", e);
        }
    }

    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setId(rs.getInt("staff_id"));
        staff.setFirstName(rs.getString("first_name"));
        staff.setLastName(rs.getString("last_name"));
        staff.setEmail(rs.getString("email"));
        staff.setPasswordHash(rs.getString("password_hash"));
        staff.setRole(rs.getString("role"));
        return staff;
    }
}
