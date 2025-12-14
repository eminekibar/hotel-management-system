package service;

import dao.NotificationDAO;
import model.Notification;

import java.util.List;

public class NotificationQueryService {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    public List<Notification> listForStaff(int staffId) {
        return notificationDAO.findForUser("staff", staffId);
    }

    public List<Notification> listForCustomer(int customerId) {
        return notificationDAO.findForUser("customer", customerId);
    }

    public void markAsRead(int notificationId) {
        notificationDAO.markAsRead(notificationId);
    }

    public void markAllAsReadForStaff(int staffId) {
        notificationDAO.markAllAsRead("staff", staffId);
    }

    public void markAllAsReadForCustomer(int customerId) {
        notificationDAO.markAllAsRead("customer", customerId);
    }
}
