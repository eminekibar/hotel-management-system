package model.reservation;

import model.room.Room;
import model.user.Customer;
import state.ReservationState;
import state.PendingState;

import java.time.LocalDate;

public class Reservation {
    private int reservationId;
    private Customer customer;
    private Room room;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalPrice;
    private ReservationState currentState;

    public Reservation() {
        this.currentState = new PendingState();
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ReservationState getCurrentState() {
        return currentState;
    }

    public void setState(ReservationState state) {
        this.currentState = state;
    }

    public void checkIn() {
        currentState.onCheckIn(this);
    }

    public void checkOut() {
        currentState.onCheckOut(this);
    }

    public void cancel() {
        currentState.onCancel(this);
    }
}
