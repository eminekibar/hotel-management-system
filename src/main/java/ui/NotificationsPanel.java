package ui;

import model.Notification;
import model.user.Customer;
import service.NotificationQueryService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationsPanel extends JPanel {

    private final JFrame owner;
    private final Customer customer;
    private final NotificationQueryService notificationQueryService;
    private final DefaultListModel<String> notificationListModel = new DefaultListModel<>();
    private List<Notification> cachedNotifications;
    private final JList<String> notificationList = new JList<>(notificationListModel);
    private final DateTimeFormatter notificationFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public NotificationsPanel(JFrame owner, Customer customer, NotificationQueryService notificationQueryService) {
        this.owner = owner;
        this.customer = customer;
        this.notificationQueryService = notificationQueryService;
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        notificationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationList.setCellRenderer(CustomerListRenderers.createNotificationRenderer());
        add(new JScrollPane(notificationList), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton markRead = new JButton("Mark Read");
        markRead.addActionListener(e -> markNotificationRead(notificationList.getSelectedIndex()));
        JButton markAll = new JButton("Mark All Read");
        markAll.addActionListener(e -> markAllNotificationsRead());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshNotifications());
        actions.add(markRead);
        actions.add(markAll);
        actions.add(refresh);
        add(actions, BorderLayout.SOUTH);
    }

    public void refreshNotifications() {
        try {
            cachedNotifications = notificationQueryService.listForCustomer(customer.getId());
            notificationListModel.clear();
            for (Notification n : cachedNotifications) {
                notificationListModel.addElement(formatNotification(n));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to load notifications: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markNotificationRead(int index) {
        if (index < 0 || cachedNotifications == null || index >= cachedNotifications.size()) {
            JOptionPane.showMessageDialog(owner, "Select a notification first");
            return;
        }
        Notification notification = cachedNotifications.get(index);
        try {
            notificationQueryService.markAsRead(notification.getId());
            refreshNotifications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to mark as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markAllNotificationsRead() {
        try {
            notificationQueryService.markAllAsReadForCustomer(customer.getId());
            refreshNotifications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to mark all as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatNotification(Notification n) {
        String time = n.getCreatedAt() == null ? "-" : LocalDateTime.ofInstant(n.getCreatedAt(), ZoneId.systemDefault()).format(notificationFormatter);
        return n.getId() + " | " + time + " | " + safe(n.getMessage()) + " | read:" + (n.isRead() ? "Y" : "N");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
