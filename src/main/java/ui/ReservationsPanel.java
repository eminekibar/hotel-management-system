package ui;

import model.reservation.Reservation;
import model.user.Customer;
import service.ReservationService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ReservationsPanel extends JPanel {

    private final JFrame owner;
    private final Customer customer;
    private final ReservationService reservationService;
    private final Runnable historyRefresher;
    private final DefaultListModel<String> reservationListModel = new DefaultListModel<>();
    private final JList<String> reservationList = new JList<>(reservationListModel);

    public ReservationsPanel(JFrame owner,
                             Customer customer,
                             ReservationService reservationService,
                             Runnable historyRefresher) {
        this.owner = owner;
        this.customer = customer;
        this.reservationService = reservationService;
        this.historyRefresher = historyRefresher;
        buildUi();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationList.setCellRenderer(CustomerListRenderers.createReservationRenderer());
        add(new JScrollPane(reservationList), BorderLayout.CENTER);
        JButton cancel = new JButton("Cancel Selected");
        cancel.addActionListener(e -> cancelSelectedReservation());
        add(cancel, BorderLayout.SOUTH);
    }

    public void refreshReservations() {
        reservationListModel.clear();
        List<Reservation> reservations = reservationService.listReservationsByCustomer(customer.getId());
        for (Reservation res : reservations) {
            reservationListModel.addElement(
                    res.getReservationId() + " | " + res.getRoom().getRoomNumber() + " | " +
                            res.getStartDate() + " - " + res.getEndDate() + " | " +
                            res.getCurrentState().getName() + " | $" + res.getTotalPrice() +
                            " | payment: " + res.getPaymentStatus()
            );
        }
    }

    private void cancelSelectedReservation() {
        int idx = reservationList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        String entry = reservationListModel.get(idx);
        int reservationId = Integer.parseInt(entry.split("\\|")[0].trim());
        try {
            reservationService.cancelReservationByCustomer(reservationId, customer.getId());
            refreshReservations();
            if (historyRefresher != null) {
                historyRefresher.run();
            }
            JOptionPane.showMessageDialog(owner, "Reservation canceled");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Cancel failed", JOptionPane.WARNING_MESSAGE);
        }
    }
}
