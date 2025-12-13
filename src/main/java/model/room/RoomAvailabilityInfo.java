package model.room;

import model.reservation.Reservation;

/**
 * Wrapper for room availability information for a given date range.
 */
public class RoomAvailabilityInfo {
    private final Room room;
    private final String availabilityLabel;
    private final boolean bookable;
    private final Reservation conflictingReservation;

    public RoomAvailabilityInfo(Room room, String availabilityLabel, boolean bookable, Reservation conflictingReservation) {
        this.room = room;
        this.availabilityLabel = availabilityLabel;
        this.bookable = bookable;
        this.conflictingReservation = conflictingReservation;
    }

    public Room getRoom() {
        return room;
    }

    public String getAvailabilityLabel() {
        return availabilityLabel;
    }

    public boolean isBookable() {
        return bookable;
    }

    public Reservation getConflictingReservation() {
        return conflictingReservation;
    }
}
