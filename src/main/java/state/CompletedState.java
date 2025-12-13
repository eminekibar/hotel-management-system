package state;

import model.reservation.Reservation;

public class CompletedState implements ReservationState {
    @Override
    public String getName() {
        return "completed";
    }

    @Override
    public void onCheckIn(Reservation reservation) {
        throw new IllegalStateException("Reservation already completed.");
    }

    @Override
    public void onCheckOut(Reservation reservation) {
        throw new IllegalStateException("Reservation already completed.");
    }

    @Override
    public void onCancel(Reservation reservation) {
        throw new IllegalStateException("Cannot cancel a completed reservation.");
    }
}
