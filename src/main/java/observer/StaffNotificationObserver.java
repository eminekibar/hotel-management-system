package observer;

import dao.NotificationDAO;

public class StaffNotificationObserver implements NotificationObserver {
    private final int staffId;
    private final NotificationDAO notificationDAO;

    public StaffNotificationObserver(int staffId, NotificationDAO notificationDAO) {
        this.staffId = staffId;
        this.notificationDAO = notificationDAO;
    }

    @Override
    public void onNotify(String message) {
        notificationDAO.create("staff", staffId, message);
    }
}
