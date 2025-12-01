package state;

import model.reservation.Reservation;

public class CheckedInState implements ReservationState {
    @Override
    public String getName() {
        return "checked_in";
    }

    @Override
    public void onCheckIn(Reservation reservation) {
        // already checked in
    }

    @Override
    public void onCheckOut(Reservation reservation) {
        reservation.setState(new CompletedState());
    }

    @Override
    public void onCancel(Reservation reservation) {
        reservation.setState(new CanceledState());
    }
}
