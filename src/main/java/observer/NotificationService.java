package observer;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private final List<NotificationObserver> observers = new ArrayList<>();

    public void registerObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyAll(String message) {
        for (NotificationObserver observer : List.copyOf(observers)) {
            observer.onNotify(message);
        }
    }
}
