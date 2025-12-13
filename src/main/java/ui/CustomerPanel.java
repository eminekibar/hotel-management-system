package ui;

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

    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JPasswordField newPasswordField = new JPasswordField();
    private final DefaultListModel<String> searchResultsModel = new DefaultListModel<>();
    private List<Room> lastSearchResults;
    private final DefaultListModel<String> reservationListModel = new DefaultListModel<>();
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();

    private final JTextField startDateField = new JTextField("2024-12-01");
    private final JTextField endDateField = new JTextField("2024-12-05");
    private final JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"standard", "suite", "family"});
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
    private final JList<String> reservationList = new JList<>(reservationListModel);

    public CustomerPanel(Customer customer) {
        this.customer = customer;
        setTitle("Customer Panel - " + customer.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);
        buildUi();
        refreshReservations();
        refreshHistory();
        setVisible(true);
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("Logged in as: " + customer.getDisplayName()), BorderLayout.WEST);
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> logout());
        header.add(logout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Profile", profilePanel());
        tabs.addTab("Search Rooms", searchPanel());
        tabs.addTab("Reservations", reservationsPanel());
        tabs.addTab("History", historyPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel profilePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(new JLabel(customer.getUsername()));
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

        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);
        JButton changePassword = new JButton("Change Password");
        changePassword.addActionListener(e -> updatePassword());
        panel.add(changePassword);

        JButton deleteAccount = new JButton("Delete Account");
        deleteAccount.addActionListener(e -> deleteAccount());
        panel.add(deleteAccount);

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

    private JPanel historyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> historyList = new JList<>(historyListModel);
        panel.add(new JScrollPane(historyList), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshHistory());
        panel.add(refresh, BorderLayout.SOUTH);
        return panel;
    }

    private void searchRooms() {
        try {
            LocalDate start = LocalDate.parse(startDateField.getText().trim());
            LocalDate end = LocalDate.parse(endDateField.getText().trim());
            String type = (String) roomTypeBox.getSelectedItem();
            int capacity = (Integer) capacitySpinner.getValue();
            lastSearchResults = roomService.search(type, capacity, start, end);
            searchResultsModel.clear();
            for (Room room : lastSearchResults) {
                searchResultsModel.addElement(room.getRoomNumber() + " | " + room.getType() + " | capacity " + room.getCapacity() + " | " + room.getPricePerNight());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid search input: " + ex.getMessage());
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
            refreshHistory();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to reserve: " + ex.getMessage());
        }
    }

    private void refreshReservations() {
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
            refreshHistory();
            JOptionPane.showMessageDialog(this, "Reservation canceled");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancel failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshHistory() {
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

    private void updatePassword() {
        String newPassword = new String(newPasswordField.getPassword()).trim();
        try {
            customerService.changePassword(customer.getId(), newPassword);
            newPasswordField.setText("");
            JOptionPane.showMessageDialog(this, "Password updated");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        dispose();
        new LoginForm();
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete your account?\nExisting reservations will stay active.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            customerService.deleteAccount(customer.getId());
            JOptionPane.showMessageDialog(this, "Account deleted. Existing reservations remain active.");
            logout();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to delete account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
