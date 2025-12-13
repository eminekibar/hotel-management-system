package service;

import dao.RoomDAO;
import factory.RoomFactory;
import model.room.Room;
import model.room.RoomAvailabilityInfo;
import dao.ReservationDAO;
import model.reservation.Reservation;

import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;

public class RoomService {

    private final RoomDAO roomDAO;
    private final ReservationDAO reservationDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
        this.reservationDAO = new ReservationDAO();
    }

    public Room addRoom(String roomNumber, String type, int capacity, double pricePerNight) {
        Room room = RoomFactory.createRoom(type);
        room.setRoomNumber(roomNumber);
        room.setCapacity(capacity);
        room.setPricePerNight(pricePerNight);
        roomDAO.create(room);
        return room;
    }

    public List<Room> listRooms() {
        return roomDAO.findAll();
    }

    public List<Room> search(String type, int capacity, LocalDate startDate, LocalDate endDate) {
        return roomDAO.search(type, capacity, startDate, endDate);
    }

    public List<RoomAvailabilityInfo> searchWithAvailability(String type, int capacity, LocalDate startDate, LocalDate endDate) {
        List<Room> rooms = roomDAO.searchAllByTypeAndCapacity(type, capacity);
        List<RoomAvailabilityInfo> results = new ArrayList<>();
        for (Room room : rooms) {
            Reservation overlap = reservationDAO.findFirstOverlapForRoom(room.getId(), startDate, endDate);
            String label = availabilityLabel(room, overlap);
            boolean bookable = overlap == null && "available".equals(room.getStatus());
            results.add(new RoomAvailabilityInfo(room, label, bookable, overlap));
        }
        return results;
    }

    private String availabilityLabel(Room room, Reservation overlap) {
        String status = room.getStatus();
        if (overlap != null) {
            return "reserved (overlaps " + overlap.getStartDate() + " - " + overlap.getEndDate() + ", status=" + overlap.getCurrentState().getName() + ")";
        }
        if ("maintenance".equals(status) || "inactive".equals(status)) {
            return status;
        }
        if ("occupied".equals(status)) {
            return "occupied";
        }
        if ("reserved".equals(status)) {
            return "reserved";
        }
        return "available";
    }

    public void updateStatus(int roomId, String status) {
        roomDAO.updateStatus(roomId, status);
    }
}
