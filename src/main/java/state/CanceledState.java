package state;

import model.reservation.Reservation;

public class CanceledState implements ReservationState {
    @Override
    public String getName() {
        return "canceled";
    }

    @Override
    public void onCheckIn(Reservation reservation) {
        // cannot check in
    }

    @Override
    public void onCheckOut(Reservation reservation) {
        // cannot check out
    }

    @Override
    public void onCancel(Reservation reservation) {
        // already canceled
    }
}
