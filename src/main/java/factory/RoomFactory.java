package factory;

import model.room.FamilyRoom;
import model.room.Room;
import model.room.StandardRoom;
import model.room.SuiteRoom;

public final class RoomFactory {

    private RoomFactory() {
    }

    public static Room createRoom(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase();
        return switch (normalized) {
            case "standard"-> new StandardRoom();
            case "suite" -> new SuiteRoom();
            case "family" -> new FamilyRoom();
            default -> new StandardRoom(); 
        };
    }
}
