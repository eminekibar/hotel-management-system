package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public final class StaffListRenderers {

    private StaffListRenderers() {
    }

    public static ListCellRenderer<String> createCustomerRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String name = part(parts, 1);
            String username = part(parts, 2).replace("user:", "").trim();
            String email = part(parts, 3).replace("email:", "").trim();
            String phone = part(parts, 4).replace("phone:", "").trim();
            String nationalId = part(parts, 5).replace("tc:", "").trim();
            String active = part(parts, 6).replace("active:", "").replace("active=", "").trim();
            boolean isActive = active.equalsIgnoreCase("y") || active.equalsIgnoreCase("yes") || active.equalsIgnoreCase("true") || active.equalsIgnoreCase("active");

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel(name + (username.isBlank() ? "" : " (" + username + ")"));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel emailLabel = new JLabel("Email: " + (email.isBlank() ? "-" : email));
            emailLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel phoneLabel = new JLabel("Phone: " + (phone.isBlank() ? "-" : phone));
            phoneLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel idLabel = new JLabel("TC: " + (nationalId.isBlank() ? "-" : nationalId));
            idLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(110, 110, 110));

            JLabel activeLabel = new JLabel(isActive ? "Active" : "Inactive");
            Color statusColor = isActive ? new Color(0, 115, 86) : new Color(150, 33, 33);
            activeLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(activeLabel, BorderLayout.EAST);

            JPanel middleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            middleLine.setOpaque(false);
            middleLine.add(emailLabel);
            middleLine.add(new JLabel("\u2022"));
            middleLine.add(phoneLabel);

            JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bottomLine.setOpaque(false);
            bottomLine.add(idLabel);

            row.add(topLine, BorderLayout.NORTH);
            row.add(middleLine, BorderLayout.CENTER);
            row.add(bottomLine, BorderLayout.SOUTH);
            return row;
        };
    }

    public static ListCellRenderer<String> createRoomRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String roomNumber = part(parts, 1).replace("room", "").trim();
            String type = part(parts, 2).replace("type:", "").trim();
            String capacity = part(parts, 3).replace("cap:", "").trim();
            String status = part(parts, 4).replace("status:", "").trim();
            String price = part(parts, 5).replace("price:", "").trim();

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel("Room " + roomNumber + (type.isBlank() ? "" : " \u2022 " + type));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel capacityLabel = new JLabel("Capacity: " + (capacity.isBlank() ? "-" : capacity));
            capacityLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel priceLabel = new JLabel("Price/Night: " + price);
            priceLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel statusLabel = new JLabel(status.isBlank() ? "status unknown" : status);
            String statusLower = status.toLowerCase();
            Color statusColor = statusLower.contains("available") ? new Color(0, 115, 86)
                    : statusLower.contains("reserve") ? new Color(181, 128, 30)
                    : statusLower.contains("maintenance") ? new Color(31, 90, 150)
                    : new Color(110, 110, 110);
            statusLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(statusLabel, BorderLayout.EAST);

            JPanel middleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            middleLine.setOpaque(false);
            middleLine.add(capacityLabel);
            middleLine.add(new JLabel("\u2022"));
            middleLine.add(priceLabel);

            row.add(topLine, BorderLayout.NORTH);
            row.add(middleLine, BorderLayout.CENTER);
            return row;
        };
    }

    public static ListCellRenderer<String> createStaffRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String name = part(parts, 1);
            String username = part(parts, 2).replace("user:", "").trim();
            String role = part(parts, 3).replace("role:", "").trim();
            String email = part(parts, 4).replace("email:", "").trim();
            String active = part(parts, 5).replace("active:", "").replace("active=", "").trim();
            boolean isActive = active.equalsIgnoreCase("y") || active.equalsIgnoreCase("yes") || active.equalsIgnoreCase("true") || active.equalsIgnoreCase("active");

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel(name + (username.isBlank() ? "" : " (" + username + ")"));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel roleLabel = new JLabel("Role: " + (role.isBlank() ? "-" : role));
            roleLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel emailLabel = new JLabel("Email: " + (email.isBlank() ? "-" : email));
            emailLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel activeLabel = new JLabel(isActive ? "Active" : "Inactive");
            Color statusColor = isActive ? new Color(0, 115, 86) : new Color(150, 33, 33);
            activeLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(activeLabel, BorderLayout.EAST);

            JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bottomLine.setOpaque(false);
            bottomLine.add(roleLabel);
            bottomLine.add(new JLabel("\u2022"));
            bottomLine.add(emailLabel);

            row.add(topLine, BorderLayout.NORTH);
            row.add(bottomLine, BorderLayout.CENTER);
            return row;
        };
    }

    public static ListCellRenderer<String> createNotificationRenderer(boolean isAdmin) {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String time = parts.length > 1 ? part(parts, 1) : "";
            String target = parts.length > 4 ? part(parts, 2) : "";
            String message = parts.length > 3 ? part(parts, parts.length > 4 ? 3 : 2) : "";
            String read = part(parts, parts.length - 1).replace("read:", "").trim();
            boolean isRead = read.equalsIgnoreCase("y") || read.equalsIgnoreCase("yes") || read.equalsIgnoreCase("true") || read.equalsIgnoreCase("1");

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel msgLabel = new JLabel(message);
            msgLabel.setFont(list.getFont().deriveFont(Font.BOLD));
            msgLabel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel timeLabel = new JLabel(time);
            timeLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel targetLabel = new JLabel(target);
            targetLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(110, 110, 110));

            JLabel readLabel = new JLabel(isRead ? "Read" : "Unread");
            Color statusColor = isRead ? new Color(110, 110, 110) : new Color(0, 115, 86);
            readLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(msgLabel, BorderLayout.CENTER);
            top.add(readLabel, BorderLayout.EAST);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bottom.setOpaque(false);
            bottom.add(timeLabel);
            if (isAdmin && !target.isBlank()) {
                bottom.add(new JLabel("\u2022"));
                bottom.add(targetLabel);
            }

            row.add(top, BorderLayout.NORTH);
            row.add(bottom, BorderLayout.CENTER);
            return row;
        };
    }

    public static ListCellRenderer<String> createStaffReservationRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String id = part(parts, 0);
            String customer = part(parts, 1);
            String room = part(parts, 2);
            String dates = part(parts, 3);
            String status = part(parts, 4).replace("status:", "").trim();
            String payment = part(parts, 5).replace("payment:", "").trim();

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel("Reservation #" + id + (room.isBlank() ? "" : " \u2022 " + room.trim()));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel customerLabel = new JLabel(customer);
            customerLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel datesLabel = new JLabel(dates);
            datesLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel statusLabel = new JLabel("Status: " + status);
            String statusLower = status.toLowerCase();
            Color statusColor = statusLower.contains("cancel") ? new Color(150, 33, 33)
                    : statusLower.contains("pending") ? new Color(181, 128, 30)
                    : statusLower.contains("check") ? new Color(0, 115, 86)
                    : new Color(0, 115, 86);
            statusLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JLabel paymentLabel = new JLabel("Payment: " + payment);
            String paymentLower = payment.toLowerCase();
            Color paymentColor = paymentLower.contains("paid") ? new Color(0, 115, 86)
                    : paymentLower.contains("refund") ? new Color(31, 90, 150)
                    : new Color(181, 128, 30);
            paymentLabel.setForeground(isSelected ? list.getSelectionForeground() : paymentColor);

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);

            JPanel middleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            middleLine.setOpaque(false);
            middleLine.add(customerLabel);
            middleLine.add(new JLabel("\u2022"));
            middleLine.add(datesLabel);

            JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            bottomLine.setOpaque(false);
            bottomLine.add(statusLabel);
            bottomLine.add(paymentLabel);

            row.add(topLine, BorderLayout.NORTH);
            row.add(middleLine, BorderLayout.CENTER);
            row.add(bottomLine, BorderLayout.SOUTH);
            return row;
        };
    }

    public static ListCellRenderer<String> createAvailableRoomRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String roomNo = part(parts, 0).replace("room:", "").trim();
            String type = part(parts, 1).replace("type:", "").trim();
            String capacity = part(parts, 2).replace("cap:", "").replace("capacity:", "").trim();
            String price = part(parts, 3).replace("price:", "").trim();
            String availability = part(parts, 4).replace("availability:", "").trim();
            String bookable = part(parts, 5).replace("bookable:", "").trim();

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel("Room " + roomNo + (type.isBlank() ? "" : " \u2022 " + type));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel priceLabel = new JLabel(price.isBlank() ? "" : price);
            priceLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(60, 60, 60));

            JLabel capacityLabel = new JLabel(capacity.isBlank() ? "" : "Capacity: " + capacity);
            capacityLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            String availabilityText = availability.isBlank()
                    ? (bookable.equalsIgnoreCase("true") ? "Available" : "Unavailable")
                    : availability;
            JLabel availabilityLabel = new JLabel(availabilityText);
            String availLower = availabilityText.toLowerCase(Locale.ROOT);
            Color statusColor = availLower.contains("unavailable") || availLower.contains("not available")
                    ? new Color(150, 33, 33)
                    : (availLower.contains("pending") || availLower.contains("hold") || availLower.contains("reserve"))
                    ? new Color(181, 128, 30)
                    : new Color(0, 115, 86);
            availabilityLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);
            availabilityLabel.setFont(list.getFont().deriveFont(list.getFont().getSize2D() - 1f));

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(priceLabel, BorderLayout.EAST);

            JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bottomLine.setOpaque(false);
            if (!capacityLabel.getText().isBlank()) {
                bottomLine.add(capacityLabel);
                bottomLine.add(new JLabel("\u2022"));
            }
            bottomLine.add(availabilityLabel);

            row.add(topLine, BorderLayout.NORTH);
            row.add(bottomLine, BorderLayout.SOUTH);
            return row;
        };
    }

    private static String part(String[] parts, int index) {
        return index < parts.length ? parts[index].trim() : "";
    }
}
