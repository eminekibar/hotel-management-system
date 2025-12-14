package service;

import dao.NotificationDAO;
import dao.ReservationActionDAO;
import dao.ReservationDAO;
import dao.RoomDAO;
import dao.StaffDAO;
import model.reservation.Reservation;
import model.room.Room;
import model.user.Customer;
import model.user.Staff;
import observer.CustomerNotificationObserver;
import observer.NotificationService;
import observer.StaffNotificationObserver;
import strategy.DefaultPricingStrategy;
import strategy.PricingStrategy;
import state.ActiveState;
import state.PendingState;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final ReservationActionDAO actionDAO;
    private final NotificationDAO notificationDAO;
    private final RoomDAO roomDAO;
    private final StaffDAO staffDAO;
    private PricingStrategy pricingStrategy;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.actionDAO = new ReservationActionDAO();
        this.notificationDAO = new NotificationDAO();
        this.roomDAO = new RoomDAO();
        this.staffDAO = new StaffDAO();
        this.pricingStrategy = new DefaultPricingStrategy();
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public Reservation createReservation(Customer customer, Room room, LocalDate start, LocalDate end) {
        return createReservation(customer, room, start, end, null);
    }

    public Reservation createReservation(Customer customer, Room room, LocalDate start, LocalDate end, Integer staffId) {
        if (customer == null || room == null) {
            throw new IllegalArgumentException("Customer and room are required");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Room capacity is invalid");
        }
        Reservation overlap = reservationDAO.findFirstOverlapForRoom(room.getId(), start, end);
        if (overlap != null) {
            throw new IllegalStateException("Room is not available for the selected dates (overlaps reservation #" + overlap.getReservationId() + ").");
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
        String actor = staffId != null ? describeStaff(staffId) : describeCustomer(customer);
        String message = "Created by " + actor + " • " + reservationSummary(reservation);
        notifyUsers(reservation, staffId, message, true);
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
        notifyUsers(reservation, staffId, "Check-in by " + describeStaff(staffId) + " • " + reservationSummary(reservation), false);
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
        notifyUsers(reservation, staffId, "Check-out by " + describeStaff(staffId) + " • " + reservationSummary(reservation), false);
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
        notifyUsers(reservation, staffId, "Payment marked PAID by " + describeStaff(staffId) + " • " + reservationSummary(reservation), false);
    }

    public void refund(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }
        reservationDAO.updatePaymentStatus(reservationId, "refunded");
        notifyUsers(reservation, staffId, "Payment REFUNDED by " + describeStaff(staffId) + " • " + reservationSummary(reservation), false);
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

    public List<Reservation> listReservationsByFilter(String customerFilter, String roomFilter, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return reservationDAO.findByFilters(customerFilter, roomFilter, startDate, endDate);
    }

    private void cancel(Reservation reservation, Integer staffId) {
        reservation.cancel();
        persistState(reservation);
        if ("paid".equals(reservation.getPaymentStatus())) {
            reservationDAO.updatePaymentStatus(reservation.getReservationId(), "refunded");
        }
        actionDAO.logCancel(reservation.getReservationId(), staffId);
        roomDAO.updateStatus(reservation.getRoom().getId(), "available");
        String actor = staffId == null ? describeCustomer(reservation.getCustomer()) : describeStaff(staffId);
        notifyUsers(reservation, staffId == null ? 0 : staffId, "Canceled by " + actor + " • " + reservationSummary(reservation), false);
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

    private void notifyUsers(Reservation reservation, Integer staffId, String message, boolean broadcastStaff) {
        NotificationService notifier = new NotificationService();
        notifier.registerObserver(new CustomerNotificationObserver(reservation.getCustomer().getId(), notificationDAO));
        Set<Integer> staffTargets = new HashSet<>();
        if (broadcastStaff || (staffId != null && staffId > 0)) {
            for (Staff s : staffDAO.findAll()) {
                if (!s.isActive()) {
                    continue;
                }
                if (!broadcastStaff && (staffId == null || s.getId() != staffId)) {
                    continue;
                }
                staffTargets.add(s.getId());
            }
        }
        if (staffId != null && staffId > 0) {
            staffTargets.add(staffId);
        }
        for (Staff s : staffDAO.findAll()) {
            if (s.isActive() && "admin".equalsIgnoreCase(s.getRole())) {
                staffTargets.add(s.getId());
            }
        }
        for (Integer id : staffTargets) {
            notifier.registerObserver(new StaffNotificationObserver(id, notificationDAO));
        }
        notifier.notifyAll(message);
    }

    private String describeStaff(Integer staffId) {
        if (staffId == null || staffId <= 0) {
            return "staff";
        }
        Staff staff = staffDAO.findById(staffId);
        if (staff == null) {
            return "staff#" + staffId;
        }
        String name = staff.getDisplayName();
        if (name == null || name.isBlank()) {
            name = staff.getUsername();
        }
        return name == null || name.isBlank() ? "staff#" + staffId : name;
    }

    private String describeCustomer(Customer customer) {
        if (customer == null) {
            return "customer";
        }
        String name = customer.getDisplayName();
        if (name == null || name.isBlank()) {
            name = customer.getUsername();
        }
        if (name == null || name.isBlank()) {
            name = "customer#" + customer.getId();
        }
        return name;
    }

    private String reservationSummary(Reservation reservation) {
        String room = reservation.getRoom() == null ? "-" : "room " + reservation.getRoom().getRoomNumber() + " (" + reservation.getRoom().getType() + ")";
        String dates = reservation.getStartDate() + " → " + reservation.getEndDate();
        String customer = describeCustomer(reservation.getCustomer());
        return "Reservation #" + reservation.getReservationId() + " • " + room + " • " + dates + " • customer " + customer;
    }
}
