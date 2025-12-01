package service;

import dao.RoomDAO;
import factory.RoomFactory;
import model.room.Room;

import java.util.List;

public class RoomService {

    private final RoomDAO roomDAO;

    public RoomService() {
        this.roomDAO = new RoomDAO();
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

    public List<Room> search(String type, int capacity) {
        return roomDAO.search(type, capacity);
    }
}
