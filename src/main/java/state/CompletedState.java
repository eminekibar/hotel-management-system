package state;

import model.reservation.Reservation;

public class CompletedState implements ReservationState {
    @Override
    public String getName() {
        return "completed";
    }

    @Override
    public void onCheckIn(Reservation reservation) {
        // no-op, already completed
    }

    @Override
    public void onCheckOut(Reservation reservation) {
        // no-op
    }

    @Override
    public void onCancel(Reservation reservation) {
        // cannot cancel completed reservation
    }
}
