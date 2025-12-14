package ui;

import model.reservation.Reservation;
import model.user.Customer;
import service.ReservationService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    private final Customer customer;
    private final ReservationService reservationService;
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();

    public HistoryPanel(Customer customer, ReservationService reservationService) {
        this.customer = customer;
        this.reservationService = reservationService;
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        JList<String> historyList = new JList<>(historyListModel);
        historyList.setCellRenderer(CustomerListRenderers.createReservationRenderer());
        add(new JScrollPane(historyList), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshHistory());
        add(refresh, BorderLayout.SOUTH);
    }

    public void refreshHistory() {
        historyListModel.clear();
        List<Reservation> reservations = reservationService.listReservationHistoryByCustomer(customer.getId());
        for (Reservation res : reservations) {
            historyListModel.addElement(
                    res.getReservationId() + " | " + res.getRoom().getRoomNumber() + " | " +
                            res.getStartDate() + " - " + res.getEndDate() + " | " +
                            res.getCurrentState().getName() + " | $" + res.getTotalPrice() +
                            " | payment: " + res.getPaymentStatus()
            );
        }
    }
}
