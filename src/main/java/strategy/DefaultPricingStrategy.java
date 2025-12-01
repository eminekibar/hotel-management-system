package strategy;

import model.room.Room;

public class DefaultPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(Room room, int nights) {
        return room.getPricePerNight() * nights;
    }
}
