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
        // not allowed
    }

    @Override
    public void onCancel(Reservation reservation) {
        reservation.setState(new CanceledState());
    }
}
