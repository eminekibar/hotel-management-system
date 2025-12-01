package state;

import model.reservation.Reservation;

public interface ReservationState {
    String getName();

    void onCheckIn(Reservation reservation);

    void onCheckOut(Reservation reservation);

    void onCancel(Reservation reservation);
}
