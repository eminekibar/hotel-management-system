package service;

import dao.NotificationDAO;
import dao.ReservationActionDAO;
import dao.ReservationDAO;
import dao.RoomDAO;
import model.reservation.Reservation;
import model.room.Room;
import model.user.Customer;
import observer.CustomerNotificationObserver;
import observer.NotificationService;
import observer.StaffNotificationObserver;
import strategy.DefaultPricingStrategy;
import strategy.PricingStrategy;
import state.ActiveState;
import state.PendingState;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final ReservationActionDAO actionDAO;
    private final NotificationDAO notificationDAO;
    private final RoomDAO roomDAO;
    private PricingStrategy pricingStrategy;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.actionDAO = new ReservationActionDAO();
        this.notificationDAO = new NotificationDAO();
        this.roomDAO = new RoomDAO();
        this.pricingStrategy = new DefaultPricingStrategy();
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public Reservation createReservation(Customer customer, Room room, LocalDate start, LocalDate end) {
        if (customer == null || room == null) {
            throw new IllegalArgumentException("Customer and room are required");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Room capacity is invalid");
        }
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setRoom(room);
        reservation.setStartDate(start);
        reservation.setEndDate(end);
        int nights = (int) ChronoUnit.DAYS.between(start, end);
        if (nights <= 0) {
            nights = 1;
        }
        reservation.setTotalPrice(pricingStrategy.calculatePrice(room, nights));
        reservation.setState(new PendingState());
        reservation.setPaymentStatus("unpaid");

        reservationDAO.create(reservation);
        roomDAO.updateStatus(room.getId(), "reserved");
        notifyUsers(reservation, 0, "Reservation created: " + reservation.getReservationId());
        return reservation;
    }

    public void cancelReservationByCustomer(int reservationId, int customerId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null || reservation.getCustomer() == null || reservation.getCustomer().getId() != customerId) {
            throw new IllegalArgumentException("Reservation not found for this customer.");
        }
        ensureCancelable(reservation);
        cancel(reservation, null);
    }

    public void cancelReservation(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return;
        }
        ensureCancelable(reservation);
        cancel(reservation, staffId);
    }

    public void checkIn(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return;
        }
        ensureCheckInAllowed(reservation);
        reservation.checkIn();
        persistState(reservation);
        actionDAO.logCheckIn(reservationId, staffId);
        roomDAO.updateStatus(reservation.getRoom().getId(), "occupied");
        notifyUsers(reservation, staffId, "Check-in completed: " + reservationId);
    }

    public void checkOut(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return;
        }
        ensureCheckedIn(reservation);
        reservation.checkOut();
        persistState(reservation);
        reservationDAO.updatePaymentStatus(reservationId, "paid");
        actionDAO.logCheckOut(reservationId, staffId);
        roomDAO.updateStatus(reservation.getRoom().getId(), "available");
        notifyUsers(reservation, staffId, "Check-out completed: " + reservationId);
    }

    public void markPaid(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }
        if ("paid".equals(reservation.getPaymentStatus())) {
            return;
        }
        reservationDAO.updatePaymentStatus(reservationId, "paid");
        notifyUsers(reservation, staffId, "Payment marked as paid for reservation " + reservationId);
    }

    public void refund(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }
        reservationDAO.updatePaymentStatus(reservationId, "refunded");
        notifyUsers(reservation, staffId, "Payment refunded for reservation " + reservationId);
    }

    public List<Reservation> listReservations() {
        return reservationDAO.findAll();
    }

    public List<Reservation> listReservationsByCustomer(int customerId) {
        return reservationDAO.findByCustomer(customerId);
    }

    public List<Reservation> listReservationHistoryByCustomer(int customerId) {
        return reservationDAO.findHistoryByCustomer(customerId);
    }

    public List<Reservation> listReservationsByFilter(String customerFilter, String roomFilter) {
        return reservationDAO.findByFilters(customerFilter, roomFilter);
    }

    private void cancel(Reservation reservation, Integer staffId) {
        reservation.cancel();
        persistState(reservation);
        if ("paid".equals(reservation.getPaymentStatus())) {
            reservationDAO.updatePaymentStatus(reservation.getReservationId(), "refunded");
        }
        actionDAO.logCancel(reservation.getReservationId(), staffId);
        roomDAO.updateStatus(reservation.getRoom().getId(), "available");
        notifyUsers(reservation, staffId == null ? 0 : staffId, "Reservation canceled: " + reservation.getReservationId());
    }

    private void ensureCancelable(Reservation reservation) {
        String state = reservation.getCurrentState().getName();
        if ("canceled".equals(state)) {
            throw new IllegalStateException("Reservation already canceled.");
        }
        if ("checked_in".equals(state) || "completed".equals(state)) {
            throw new IllegalStateException("Reservation cannot be canceled after check-in or completion.");
        }
    }

    private void ensureCheckedIn(Reservation reservation) {
        String state = reservation.getCurrentState().getName();
        if (!"checked_in".equals(state)) {
            throw new IllegalStateException("Check-out allowed only after check-in.");
        }
    }

    private void ensureCheckInAllowed(Reservation reservation) {
        String state = reservation.getCurrentState().getName();
        if ("canceled".equals(state) || "completed".equals(state) || "checked_in".equals(state)) {
            throw new IllegalStateException("Cannot check-in in the current state: " + state);
        }
    }

    private void persistState(Reservation reservation) {
        reservationDAO.updateStatus(reservation.getReservationId(), reservation.getCurrentState().getName());
    }

    private void notifyUsers(Reservation reservation, int staffId, String message) {
        NotificationService notifier = new NotificationService();
        notifier.registerObserver(new CustomerNotificationObserver(reservation.getCustomer().getId(), notificationDAO));
        if (staffId > 0) {
            notifier.registerObserver(new StaffNotificationObserver(staffId, notificationDAO));
        }
        notifier.notifyAll(message);
    }
}
