package state;

import model.reservation.Reservation;

public class ActiveState implements ReservationState {
    @Override
    public String getName() {
        return "active";
    }

    @Override
    public void onCheckIn(Reservation reservation) {
        reservation.setState(new CheckedInState());
    }

    @Override
    public void onCheckOut(Reservation reservation) {
        // cannot checkout before check-in
    }

    @Override
    public void onCancel(Reservation reservation) {
        reservation.setState(new CanceledState());
    }
}
