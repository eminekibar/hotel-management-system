package ui;

import model.reservation.Reservation;
import model.user.Customer;
import service.ReservationService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CustomerDetailDialog extends JDialog {

    private final DefaultListModel<String> reservationsModel = new DefaultListModel<>();
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();

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
        tabs.addTab("Reservations", new JScrollPane(reservationsList));

        JList<String> historyList = new JList<>(historyModel);
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
        return res.getReservationId() + " | room " + res.getRoom().getRoomNumber() +
                " | " + res.getStartDate() + " - " + res.getEndDate() +
                " | status: " + res.getCurrentState().getName() +
                " | payment: " + res.getPaymentStatus();
    }
}
