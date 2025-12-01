package dao;

import database.DatabaseConnection;
import model.reservation.Reservation;
import model.room.Room;
import model.user.Customer;
import state.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private final Connection connection;
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    public ReservationDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public int create(Reservation reservation) {
        String sql = "INSERT INTO reservations (customer_id, room_id, start_date, end_date, total_price, status) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getCustomer().getId());
            ps.setInt(2, reservation.getRoom().getId());
            ps.setDate(3, Date.valueOf(reservation.getStartDate()));
            ps.setDate(4, Date.valueOf(reservation.getEndDate()));
            ps.setDouble(5, reservation.getTotalPrice());
            ps.setString(6, reservation.getCurrentState().getName());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    reservation.setReservationId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create reservation", e);
        }
        return -1;
    }

    public Reservation findById(int id) {
        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservation", e);
        }
        return null;
    }

    public List<Reservation> findByCustomer(int customerId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE customer_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list reservations for customer", e);
        }
        return reservations;
    }

    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list reservations", e);
        }
        return reservations;
    }

    public void updateStatus(int reservationId, String status) {
        String sql = "UPDATE reservations SET status=? WHERE reservation_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reservation status", e);
        }
    }

    public void cancel(int reservationId) {
        updateStatus(reservationId, "canceled");
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getInt("reservation_id"));

        int customerId = rs.getInt("customer_id");
        Customer customer = customerDAO.findById(customerId);
        reservation.setCustomer(customer);

        int roomId = rs.getInt("room_id");
        Room room = roomDAO.findById(roomId);
        reservation.setRoom(room);

        LocalDate startDate = rs.getDate("start_date").toLocalDate();
        LocalDate endDate = rs.getDate("end_date").toLocalDate();
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setTotalPrice(rs.getDouble("total_price"));
        reservation.setState(stateFrom(rs.getString("status")));
        return reservation;
    }

    private ReservationState stateFrom(String status) {
        return switch (status) {
            case "pending" -> new PendingState();
            case "active" -> new ActiveState();
            case "checked_in" -> new CheckedInState();
            case "completed" -> new CompletedState();
            case "canceled" -> new CanceledState();
            default -> new PendingState();
        };
    }
}
