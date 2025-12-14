package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public final class CustomerListRenderers {

    private CustomerListRenderers() {
    }

    public static ListCellRenderer<String> createRoomResultRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String roomNo = part(parts, 0);
            String type = capitalize(part(parts, 1));
            String capacity = part(parts, 2);
            String price = part(parts, 3);
            String availability = part(parts, 4);

            String capacityNumber = capacity.toLowerCase().startsWith("capacity")
                    ? capacity.substring("capacity".length()).trim()
                    : capacity;
            String guestLabel = capacityNumber.isBlank()
                    ? ""
                    : capacityNumber + " guest" + (capacityNumber.equals("1") ? "" : "s");

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel("Room " + roomNo + (type.isBlank() ? "" : " \u2022 " + type) +
                    (guestLabel.isBlank() ? "" : " \u2022 " + guestLabel));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel priceLabel = new JLabel(price.isBlank() ? "" : price + " / night");
            priceLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(60, 60, 60));

            JLabel availabilityLabel = new JLabel(availability);
            String availLower = availability.toLowerCase(Locale.ROOT);
            Color statusColor = availLower.contains("unavailable")
                    ? new Color(162, 30, 30)
                    : availLower.contains("pending")
                    ? new Color(181, 128, 30)
                    : new Color(0, 128, 96);
            availabilityLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);
            availabilityLabel.setFont(list.getFont().deriveFont(list.getFont().getSize2D() - 1f));

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(priceLabel, BorderLayout.EAST);

            JPanel bottomLine = new JPanel(new BorderLayout());
            bottomLine.setOpaque(false);
            bottomLine.add(availabilityLabel, BorderLayout.WEST);

            row.add(topLine, BorderLayout.NORTH);
            row.add(bottomLine, BorderLayout.SOUTH);
            return row;
        };
    }

    public static ListCellRenderer<String> createReservationRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String id = part(parts, 0);
            String roomNo = part(parts, 1);
            String dates = part(parts, 2);
            String status = part(parts, 3);
            String price = part(parts, 4);
            String payment = part(parts, 5);

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel("Reservation #" + id + (roomNo.isBlank() ? "" : " \u2022 Room " + roomNo));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel datesLabel = new JLabel(dates);
            datesLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel statusLabel = new JLabel(status);
            String statusLower = status.toLowerCase(Locale.ROOT);
            Color statusColor = statusLower.contains("cancel") ? new Color(150, 33, 33)
                    : statusLower.contains("pending") ? new Color(181, 128, 30)
                    : new Color(0, 115, 86);
            statusLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JLabel paymentLabel = new JLabel(payment);
            String paymentLower = payment.toLowerCase(Locale.ROOT);
            Color paymentColor = paymentLower.contains("paid") ? new Color(0, 115, 86)
                    : paymentLower.contains("refund") ? new Color(31, 90, 150)
                    : new Color(181, 128, 30);
            paymentLabel.setForeground(isSelected ? list.getSelectionForeground() : paymentColor);

            JLabel priceLabel = new JLabel(price);
            priceLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(60, 60, 60));

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(priceLabel, BorderLayout.EAST);

            JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            bottomLine.setOpaque(false);
            bottomLine.add(datesLabel);
            if (!status.isBlank()) {
                bottomLine.add(statusLabel);
            }
            if (!payment.isBlank()) {
                bottomLine.add(paymentLabel);
            }

            row.add(topLine, BorderLayout.NORTH);
            row.add(bottomLine, BorderLayout.SOUTH);
            return row;
        };
    }

    public static ListCellRenderer<String> createNotificationRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String time = part(parts, 1);
            String message = part(parts, 2);
            String read = part(parts, 3).replace("read:", "").trim();
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

            row.add(top, BorderLayout.NORTH);
            row.add(bottom, BorderLayout.CENTER);
            return row;
        };
    }

    private static String part(String[] parts, int index) {
        return index < parts.length ? parts[index].trim() : "";
    }

    private static String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
