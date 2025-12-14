package ui;

import model.Notification;
import model.user.Staff;
import service.NotificationQueryService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StaffNotificationsPanel extends JPanel {

    private final JFrame owner;
    private final Staff staff;
    private final NotificationQueryService notificationQueryService;
    private final boolean isAdmin;
    private final DefaultListModel<String> notificationListModel = new DefaultListModel<>();
    private List<Notification> cachedNotifications;
    private final JList<String> notificationList = new JList<>(notificationListModel);
    private List<List<Notification>> adminGroupedNotifications;
    private final DateTimeFormatter notificationTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StaffNotificationsPanel(JFrame owner, Staff staff, NotificationQueryService notificationQueryService, boolean isAdmin) {
        this.owner = owner;
        this.staff = staff;
        this.notificationQueryService = notificationQueryService;
        this.isAdmin = isAdmin;
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        notificationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationList.setCellRenderer(StaffListRenderers.createNotificationRenderer(isAdmin));
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
            if (isAdmin) {
                cachedNotifications = notificationQueryService.listAll();
                renderAdminNotifications(cachedNotifications);
            } else {
                cachedNotifications = notificationQueryService.listForStaff(staff.getId());
                adminGroupedNotifications = null;
                notificationListModel.clear();
                for (Notification n : cachedNotifications) {
                    notificationListModel.addElement(formatNotification(n));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to load notifications: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markNotificationRead(int index) {
        if (isAdmin && adminGroupedNotifications != null) {
            if (index < 0 || index >= adminGroupedNotifications.size()) {
                JOptionPane.showMessageDialog(owner, "Select a notification first");
                return;
            }
            try {
                for (Notification n : adminGroupedNotifications.get(index)) {
                    notificationQueryService.markAsRead(n.getId());
                }
                refreshNotifications();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(owner, "Failed to mark as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
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
            if (isAdmin) {
                notificationQueryService.markAllAsReadAll();
            } else {
                notificationQueryService.markAllAsReadForStaff(staff.getId());
            }
            refreshNotifications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to mark all as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderAdminNotifications(List<Notification> notifications) {
        adminGroupedNotifications = new ArrayList<>();
        notificationListModel.clear();
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        java.util.Map<String, List<Notification>> groups = new java.util.LinkedHashMap<>();
        for (Notification n : notifications) {
            String timeKey = n.getCreatedAt() == null ? "-" : String.valueOf(n.getCreatedAt().getEpochSecond());
            String key = n.getMessage() + "|" + timeKey;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(n);
        }
        int i = 1;
        for (List<Notification> group : groups.values()) {
            adminGroupedNotifications.add(group);
            Notification first = group.get(0);
            boolean anyUnread = group.stream().anyMatch(n -> !n.isRead());
            String time = first.getCreatedAt() == null ? "-" : LocalDateTime.ofInstant(first.getCreatedAt(), ZoneId.systemDefault()).format(notificationTimeFormatter);
            String targets = group.stream()
                    .map(n -> (n.getUserType() == null ? "-" : n.getUserType()) + "#" + n.getUserId())
                    .distinct()
                    .sorted()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("-");
            String formatted = "G" + i + " | " + time + " | targets: " + targets + " | " + safe(first.getMessage()) + " | read:" + (anyUnread ? "N" : "Y");
            notificationListModel.addElement(formatted);
            i++;
        }
    }

    private String formatNotification(Notification n) {
        String time = n.getCreatedAt() == null ? "-" : LocalDateTime.ofInstant(n.getCreatedAt(), ZoneId.systemDefault()).format(notificationTimeFormatter);
        if (isAdmin) {
            String target = n.getUserType() == null ? "-" : n.getUserType() + "#" + n.getUserId();
            return n.getId() + " | " + time + " | " + target + " | " + safe(n.getMessage()) + " | read:" + (n.isRead() ? "Y" : "N");
        }
        return n.getId() + " | " + time + " | " + safe(n.getMessage()) + " | read:" + (n.isRead() ? "Y" : "N");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
