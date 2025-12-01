package observer;

import dao.NotificationDAO;

public class CustomerNotificationObserver implements NotificationObserver {
    private final int customerId;
    private final NotificationDAO notificationDAO;

    public CustomerNotificationObserver(int customerId, NotificationDAO notificationDAO) {
        this.customerId = customerId;
        this.notificationDAO = notificationDAO;
    }

    @Override
    public void onNotify(String message) {
        notificationDAO.create("customer", customerId, message);
    }
}
