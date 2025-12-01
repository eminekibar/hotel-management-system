package service;

import dao.NotificationDAO;
import dao.ReservationActionDAO;
import dao.ReservationDAO;
import model.reservation.Reservation;
import model.room.Room;
import model.user.Customer;
import observer.CustomerNotificationObserver;
import observer.NotificationService;
import observer.StaffNotificationObserver;
import strategy.DefaultPricingStrategy;
import strategy.PricingStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservationService {

    private final ReservationDAO reservationDAO;
    private final ReservationActionDAO actionDAO;
    private final NotificationDAO notificationDAO;
    private PricingStrategy pricingStrategy;
    private final NotificationService notificationService;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
        this.actionDAO = new ReservationActionDAO();
        this.notificationDAO = new NotificationDAO();
        this.pricingStrategy = new DefaultPricingStrategy();
        this.notificationService = new NotificationService();
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public Reservation createReservation(Customer customer, Room room, LocalDate start, LocalDate end) {
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setRoom(room);
        reservation.setStartDate(start);
        reservation.setEndDate(end);
        int nights = (int) ChronoUnit.DAYS.between(start, end);
        reservation.setTotalPrice(pricingStrategy.calculatePrice(room, nights));

        reservationDAO.create(reservation);
        notificationService.registerObserver(new CustomerNotificationObserver(customer.getId(), notificationDAO));
        notificationService.notifyAll("Rezervasyonunuz oluşturuldu. No: " + reservation.getReservationId());
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
        registerObservers(reservation, staffId);
        notificationService.notifyAll("Rezervasyon iptal edildi. No: " + reservationId);
    }

    public void checkIn(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return;
        }
        reservation.checkIn();
        persistState(reservation);
        actionDAO.logCheckIn(reservationId, staffId);
        registerObservers(reservation, staffId);
        notificationService.notifyAll("Check-in tamamlandı. No: " + reservationId);
    }

    public void checkOut(int reservationId, int staffId) {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return;
        }
        reservation.checkOut();
        persistState(reservation);
        actionDAO.logCheckOut(reservationId, staffId);
        registerObservers(reservation, staffId);
        notificationService.notifyAll("Check-out tamamlandı. No: " + reservationId);
    }

    public List<Reservation> listReservations() {
        return reservationDAO.findAll();
    }

    public List<Reservation> listReservationsByCustomer(int customerId) {
        return reservationDAO.findByCustomer(customerId);
    }

    private void persistState(Reservation reservation) {
        reservationDAO.updateStatus(reservation.getReservationId(), reservation.getCurrentState().getName());
    }

    private void registerObservers(Reservation reservation, int staffId) {
        notificationService.registerObserver(new CustomerNotificationObserver(reservation.getCustomer().getId(), notificationDAO));
        if (staffId > 0) {
            notificationService.registerObserver(new StaffNotificationObserver(staffId, notificationDAO));
        }
    }
}
