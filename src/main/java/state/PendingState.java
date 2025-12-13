package state;

import model.reservation.Reservation;

public class PendingState implements ReservationState {
    @Override
    public String getName() {
        return "pending";
    }

    @Override
    public void onCheckIn(Reservation reservation) {
        reservation.setState(new CheckedInState());
    }

    @Override
    public void onCheckOut(Reservation reservation) {
        throw new IllegalStateException("Cannot check-out before check-in.");
    }

    @Override
    public void onCancel(Reservation reservation) {
        reservation.setState(new CanceledState());
    }
}
