package ui;

import model.reservation.Reservation;
import model.user.Customer;
import service.ReservationService;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerDetailDialog extends JDialog {

    private final DefaultListModel<String> reservationsModel = new DefaultListModel<>();
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CustomerDetailDialog(Frame owner, Customer customer, ReservationService reservationService) {
        super(owner, "Customer Details - " + customer.getDisplayName(), true);
        setSize(700, 500);
        setLocationRelativeTo(owner);
        buildUi(customer, reservationService);
    }

    private void buildUi(Customer customer, ReservationService reservationService) {
        JPanel main = new JPanel(new BorderLayout(8, 8));
        JPanel header = new JPanel(new GridLayout(0, 1));
        header.add(new JLabel("ID: " + customer.getId()));
        header.add(new JLabel("Name: " + customer.getDisplayName()));
        header.add(new JLabel("Username: " + customer.getUsername()));
        header.add(new JLabel("Email: " + customer.getEmail()));
        header.add(new JLabel("Phone: " + customer.getPhone()));
        header.add(new JLabel("National ID: " + customer.getNationalId()));
        main.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        JList<String> reservationsList = new JList<>(reservationsModel);
        reservationsList.setCellRenderer(createReservationRenderer());
        tabs.addTab("Reservations", new JScrollPane(reservationsList));

        JList<String> historyList = new JList<>(historyModel);
        historyList.setCellRenderer(createReservationRenderer());
        tabs.addTab("History", new JScrollPane(historyList));

        main.add(tabs, BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(close);
        main.add(footer, BorderLayout.SOUTH);

        add(main);

        loadReservations(customer, reservationService);
    }

    private void loadReservations(Customer customer, ReservationService reservationService) {
        reservationsModel.clear();
        historyModel.clear();
        List<Reservation> reservations = reservationService.listReservationsByCustomer(customer.getId());
        for (Reservation res : reservations) {
            reservationsModel.addElement(formatReservation(res));
        }
        List<Reservation> history = reservationService.listReservationHistoryByCustomer(customer.getId());
        for (Reservation res : history) {
            historyModel.addElement(formatReservation(res));
        }
    }

    private String formatReservation(Reservation res) {
        String dates = dateFormatter.format(res.getStartDate()) + " - " + dateFormatter.format(res.getEndDate());
        return res.getReservationId() + " | room " + res.getRoom().getRoomNumber() +
                " (" + res.getRoom().getType() + ")" +
                " | " + dates +
                " | status: " + res.getCurrentState().getName() +
                " | payment: " + res.getPaymentStatus();
    }

    private ListCellRenderer<String> createReservationRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String id = part(parts, 0);
            String room = part(parts, 1);
            String dates = part(parts, 2);
            String status = part(parts, 3).replace("status:", "").trim();
            String payment = part(parts, 4).replace("payment:", "").trim();

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

            JLabel datesLabel = new JLabel(dates);
            datesLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel statusLabel = new JLabel("Status: " + status);
            String statusLower = status.toLowerCase();
            Color statusColor = statusLower.contains("cancel") ? new Color(150, 33, 33)
                    : statusLower.contains("pending") ? new Color(181, 128, 30)
                    : statusLower.contains("check") ? new Color(0, 115, 86);
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

    private String part(String[] parts, int index) {
        return index < parts.length ? parts[index].trim() : "";
    }
}
