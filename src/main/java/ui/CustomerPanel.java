package ui;

import dao.NotificationDAO;
import model.Notification;
import model.reservation.Reservation;
import model.room.Room;
import model.user.Customer;
import service.CustomerService;
import service.ReservationService;
import service.RoomService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class CustomerPanel extends JFrame {

    private final Customer customer;
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final ReservationService reservationService = new ReservationService();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final DefaultListModel<String> searchResultsModel = new DefaultListModel<>();
    private List<Room> lastSearchResults;
    private final DefaultListModel<String> reservationListModel = new DefaultListModel<>();
    private final DefaultListModel<String> notificationListModel = new DefaultListModel<>();

    private final JTextField startDateField = new JTextField("2024-12-01");
    private final JTextField endDateField = new JTextField("2024-12-05");
    private final JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"standard", "suite", "family"});
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
    private final JList<String> reservationList = new JList<>(reservationListModel);

    public CustomerPanel(Customer customer) {
        this.customer = customer;
        setTitle("Customer Panel - " + customer.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        buildUi();
        refreshReservations();
        refreshNotifications();
        setVisible(true);
    }

    private void buildUi() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Profile", profilePanel());
        tabs.addTab("Search Rooms", searchPanel());
        tabs.addTab("Reservations", reservationsPanel());
        tabs.addTab("Notifications", notificationsPanel());
        add(tabs);
    }

    private JPanel profilePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("First Name:"));
        panel.add(new JLabel(customer.getFirstName()));
        panel.add(new JLabel("Last Name:"));
        panel.add(new JLabel(customer.getLastName()));
        panel.add(new JLabel("National ID:"));
        panel.add(new JLabel(customer.getNationalId()));
        panel.add(new JLabel("Email:"));
        emailField.setText(customer.getEmail());
        panel.add(emailField);
        panel.add(new JLabel("Phone:"));
        phoneField.setText(customer.getPhone());
        panel.add(phoneField);
        JButton save = new JButton("Update Contact");
        save.addActionListener(e -> {
            customer.setEmail(emailField.getText().trim());
            customer.setPhone(phoneField.getText().trim());
            customerService.updateProfile(customer);
            JOptionPane.showMessageDialog(this, "Profile updated");
        });
        panel.add(save);
        return panel;
    }

    private JPanel searchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Start Date (yyyy-MM-dd):"));
        form.add(startDateField);
        form.add(new JLabel("End Date (yyyy-MM-dd):"));
        form.add(endDateField);
        form.add(new JLabel("Room Type:"));
        form.add(roomTypeBox);
        form.add(new JLabel("Capacity:"));
        form.add(capacitySpinner);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchRooms());
        form.add(searchButton);
        panel.add(form, BorderLayout.NORTH);

        JList<String> resultList = new JList<>(searchResultsModel);
        panel.add(new JScrollPane(resultList), BorderLayout.CENTER);

        JButton reserveButton = new JButton("Reserve Selected");
        reserveButton.addActionListener(e -> reserveSelected(resultList.getSelectedIndex()));
        panel.add(reserveButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel reservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(reservationList), BorderLayout.CENTER);
        JButton cancel = new JButton("Cancel Selected");
        cancel.addActionListener(e -> cancelSelectedReservation());
        panel.add(cancel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel notificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> notificationList = new JList<>(notificationListModel);
        panel.add(new JScrollPane(notificationList), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshNotifications());
        panel.add(refresh, BorderLayout.SOUTH);
        return panel;
    }

    private void searchRooms() {
        String type = (String) roomTypeBox.getSelectedItem();
        int capacity = (Integer) capacitySpinner.getValue();
        lastSearchResults = roomService.search(type, capacity);
        searchResultsModel.clear();
        for (Room room : lastSearchResults) {
            searchResultsModel.addElement(room.getRoomNumber() + " | " + room.getType() + " | capacity " + room.getCapacity() + " | " + room.getPricePerNight());
        }
    }

    private void reserveSelected(int index) {
        if (index < 0 || lastSearchResults == null || lastSearchResults.isEmpty()) {
            return;
        }
        try {
            LocalDate start = LocalDate.parse(startDateField.getText().trim());
            LocalDate end = LocalDate.parse(endDateField.getText().trim());
            Room room = lastSearchResults.get(index);
            Reservation reservation = reservationService.createReservation(customer, room, start, end);
            JOptionPane.showMessageDialog(this, "Reservation created: " + reservation.getReservationId());
            refreshReservations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to reserve: " + ex.getMessage());
        }
    }

    private void refreshReservations() {
        reservationListModel.clear();
        List<Reservation> reservations = reservationService.listReservationsByCustomer(customer.getId());
        for (Reservation res : reservations) {
            reservationListModel.addElement(res.getReservationId() + " | " + res.getStartDate() + " - " + res.getEndDate() + " | " + res.getCurrentState().getName());
        }
    }

    private void cancelSelectedReservation() {
        int idx = reservationList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        String entry = reservationListModel.get(idx);
        int reservationId = Integer.parseInt(entry.split("\\|")[0].trim());
        reservationService.cancelReservation(reservationId, 0);
        refreshReservations();
    }

    private void refreshNotifications() {
        notificationListModel.clear();
        List<Notification> notifications = notificationDAO.findForUser("customer", customer.getId());
        for (Notification notification : notifications) {
            notificationListModel.addElement(notification.getMessage() + " @ " + notification.getCreatedAt());
        }
    }
}
