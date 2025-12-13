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
        String sql = "INSERT INTO reservations (customer_id, room_id, start_date, end_date, total_price, payment_status, status) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getCustomer().getId());
            ps.setInt(2, reservation.getRoom().getId());
            ps.setDate(3, Date.valueOf(reservation.getStartDate()));
            ps.setDate(4, Date.valueOf(reservation.getEndDate()));
            ps.setDouble(5, reservation.getTotalPrice());
            ps.setString(6, reservation.getPaymentStatus());
            ps.setString(7, reservation.getCurrentState().getName());
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
        String sql = "SELECT * FROM reservations WHERE customer_id = ? ORDER BY created_at DESC";
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

    public List<Reservation> findHistoryByCustomer(int customerId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = """
                SELECT * FROM reservations
                WHERE customer_id = ?
                  AND (status IN ('completed','canceled') OR end_date < CURRENT_DATE)
                ORDER BY end_date DESC
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list reservation history", e);
        }
        return reservations;
    }

    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY created_at DESC";
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

    public List<Reservation> findByFilters(String customerFilter, String roomFilter, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<Reservation> reservations = new ArrayList<>();
        List<String> clauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (customerFilter != null && !customerFilter.isBlank()) {
            clauses.add("customer_id IN (SELECT customer_id FROM customers WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR national_id LIKE ? OR username LIKE ?)");
            String like = "%" + customerFilter + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (roomFilter != null && !roomFilter.isBlank()) {
            clauses.add("room_id IN (SELECT room_id FROM rooms WHERE room_number LIKE ? OR room_type LIKE ?)");
            String like = "%" + roomFilter + "%";
            params.add(like);
            params.add(like);
        }
        if (startDate != null && endDate != null) {
            clauses.add("NOT (end_date < ? OR start_date > ?)");
            params.add(java.sql.Date.valueOf(startDate));
            params.add(java.sql.Date.valueOf(endDate));
        } else if (startDate != null) {
            clauses.add("end_date >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        } else if (endDate != null) {
            clauses.add("start_date <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM reservations");
        if (!clauses.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", clauses));
        }
        sql.append(" ORDER BY created_at DESC");
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) {
                    ps.setString(i + 1, s);
                } else if (p instanceof java.sql.Date d) {
                    ps.setDate(i + 1, d);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to filter reservations", e);
        }
        return reservations;
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
        reservation.setPaymentStatus(rs.getString("payment_status"));
        reservation.setState(stateFrom(rs.getString("status")));
        return reservation;
    }

    public void updatePaymentStatus(int reservationId, String paymentStatus) {
        String sql = "UPDATE reservations SET payment_status=? WHERE reservation_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update payment status", e);
        }
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

    public Reservation findFirstOverlapForRoom(int roomId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT * FROM reservations
                WHERE room_id = ?
                  AND status NOT IN ('canceled')
                  AND NOT (end_date <= ? OR start_date >= ?)
                ORDER BY start_date
                LIMIT 1
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check room availability overlap", e);
        }
        return null;
    }
}
