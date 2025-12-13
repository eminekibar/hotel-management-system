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
        reservation.setState(new ActiveState());

        reservationDAO.create(reservation);
        roomDAO.updateStatus(room.getId(), "reserved");
        notifyUsers(reservation, 0, "Reservation created: " + reservation.getReservationId());
        return reservation;
    }

    public void cancelReservation(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return;
        }
        reservation.cancel();
        persistState(reservation);
        actionDAO.logCancel(reservationId, staffId);
        roomDAO.updateStatus(reservation.getRoom().getId(), "available");
        notifyUsers(reservation, staffId, "Reservation canceled: " + reservationId);
    }

    public void checkIn(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return;
        }
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
        reservation.checkOut();
        persistState(reservation);
        actionDAO.logCheckOut(reservationId, staffId);
        roomDAO.updateStatus(reservation.getRoom().getId(), "available");
        notifyUsers(reservation, staffId, "Check-out completed: " + reservationId);
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
