package strategy;

import model.room.Room;

public interface PricingStrategy {
    double calculatePrice(Room room, int nights);
}
