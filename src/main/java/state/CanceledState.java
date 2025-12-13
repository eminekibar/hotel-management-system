package state;

import model.reservation.Reservation;

public class CanceledState implements ReservationState {
    @Override
    public String getName() {
        return "canceled";
    }

    @Override
    public void onCheckIn(Reservation reservation) {
        throw new IllegalStateException("Reservation is canceled.");
    }

    @Override
    public void onCheckOut(Reservation reservation) {
        throw new IllegalStateException("Reservation is canceled.");
    }

    @Override
    public void onCancel(Reservation reservation) {
        throw new IllegalStateException("Reservation is already canceled.");
    }
}
