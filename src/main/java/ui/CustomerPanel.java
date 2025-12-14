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
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

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

    private final JTextField startDateField = new JTextField();
    private final JTextField endDateField = new JTextField();
    private final JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"standard", "suite", "family"});
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
    private final JList<String> reservationList = new JList<>(reservationListModel);
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");

    public CustomerPanel(Customer customer) {
        this.customer = customer;
        setTitle("Customer Panel - " + customer.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);
        buildUi();
        refreshReservations();
        refreshHistory();
        setDefaultDates();
        setVisible(true);
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, " + customer.getDisplayName());
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 16f));
        welcome.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        header.add(welcome, BorderLayout.WEST);
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> logout());
        header.add(logout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book a Stay", searchPanel());
        tabs.addTab("My Reservations", reservationsPanel());
        tabs.addTab("History", historyPanel());
        tabs.addTab("Profile", profilePanel());
        tabs.setSelectedIndex(0);
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
        form.add(new JLabel("Check-in date:"));
        form.add(wrapWithDatePicker(startDateField));
        form.add(new JLabel("Check-out date:"));
        form.add(wrapWithDatePicker(endDateField));
        form.add(new JLabel("Room type:"));
        form.add(roomTypeBox);
        form.add(new JLabel("Guests:"));
        form.add(capacitySpinner);
        JButton searchButton = new JButton("Find Rooms");
        searchButton.addActionListener(e -> searchRooms());
        form.add(searchButton);
        panel.add(form, BorderLayout.NORTH);

        JList<String> resultList = new JList<>(searchResultsModel);
        panel.add(new JScrollPane(resultList), BorderLayout.CENTER);

        JButton reserveButton = new JButton("Book Selected");
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
        LocalDate start = parseDateField(startDateField, "Check-in date");
        LocalDate end = parseDateField(endDateField, "Check-out date");
        if (start == null || end == null) {
            return;
        }
        if (!end.isAfter(start)) {
            JOptionPane.showMessageDialog(this, "Check-out must be after check-in.");
            return;
        }
        try {
            String type = (String) roomTypeBox.getSelectedItem();
            int capacity = (Integer) capacitySpinner.getValue();
            java.util.List<model.room.RoomAvailabilityInfo> availabilityList = roomService.searchWithAvailability(type, capacity, start, end);
            lastSearchResults = new java.util.ArrayList<>();
            searchResultsModel.clear();
            for (int i = 0; i < availabilityList.size(); i++) {
                var info = availabilityList.get(i);
                Room room = info.getRoom();
                lastSearchResults.add(room);
                searchResultsModel.addElement(
                        room.getRoomNumber() + " | " + room.getType() +
                                " | capacity " + room.getCapacity() +
                                " | $" + room.getPricePerNight() +
                                " | " + info.getAvailabilityLabel()
                );
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
            LocalDate start = parseDateField(startDateField, "Check-in date");
            LocalDate end = parseDateField(endDateField, "Check-out date");
            if (start == null || end == null) {
                return;
            }
            if (!end.isAfter(start)) {
                JOptionPane.showMessageDialog(this, "Check-out must be after check-in.");
                return;
            }
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
                "Are you sure you want to deactivate your account?\nExisting reservations will stay active.",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            customerService.deleteAccount(customer.getId());
            JOptionPane.showMessageDialog(this, "Account deactivated. Existing reservations remain active.");
            logout();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to deactivate account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setDefaultDates() {
        LocalDate today = LocalDate.now();
        setDateField(startDateField, today.plusDays(1));
        setDateField(endDateField, today.plusDays(3));
    }

    private JPanel wrapWithDatePicker(JTextField field) {
        field.setEditable(false);
        field.setColumns(12);
        field.setHorizontalAlignment(JTextField.CENTER);
        JButton button = new JButton("\uD83D\uDCC5");
        button.setMargin(new Insets(2, 6, 2, 6));
        button.addActionListener(e -> openCalendar(field));
        JPanel wrapper = new JPanel(new BorderLayout(4, 0));
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(button, BorderLayout.EAST);
        return wrapper;
    }

    private void openCalendar(JTextField targetField) {
        LocalDate baseDate = parseDateField(targetField, null);
        if (baseDate == null) {
            baseDate = LocalDate.now();
        }
        JDialog dialog = new JDialog(this, "Select date", true);
        dialog.setLayout(new BorderLayout(8, 8));
        JPanel monthNav = new JPanel(new BorderLayout());
        JButton prevMonth = new JButton("<");
        JButton nextMonth = new JButton(">");
        JButton prevYear = new JButton("<<");
        JButton nextYear = new JButton(">>");
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 13f));
        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        west.add(prevYear);
        west.add(prevMonth);
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        east.add(nextMonth);
        east.add(nextYear);
        monthNav.add(west, BorderLayout.WEST);
        monthNav.add(monthLabel, BorderLayout.CENTER);
        monthNav.add(east, BorderLayout.EAST);

        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        JPanel container = new JPanel(new BorderLayout(0, 6));
        container.add(buildWeekHeader(), BorderLayout.NORTH);
        container.add(daysPanel, BorderLayout.CENTER);

        dialog.add(monthNav, BorderLayout.NORTH);
        dialog.add(container, BorderLayout.CENTER);
        dialog.setSize(320, 260);
        dialog.setLocationRelativeTo(this);

        final LocalDate[] currentMonth = {baseDate.withDayOfMonth(1)};
        Runnable render = () -> {
            monthLabel.setText(currentMonth[0].format(monthFormatter));
            daysPanel.removeAll();
            int startOffset = (currentMonth[0].getDayOfWeek().getValue() + 6) % 7; // Monday=0..Sunday=6
            for (int i = 0; i < startOffset; i++) {
                daysPanel.add(new JLabel(""));
            }
            int length = currentMonth[0].lengthOfMonth();
            for (int d = 1; d <= length; d++) {
                LocalDate date = currentMonth[0].withDayOfMonth(d);
                JButton dayBtn = new JButton(String.valueOf(d));
                dayBtn.setMargin(new Insets(2, 2, 2, 2));
                dayBtn.addActionListener(e -> {
                    setDateField(targetField, date);
                    dialog.dispose();
                });
                daysPanel.add(dayBtn);
            }
            daysPanel.revalidate();
            daysPanel.repaint();
        };
        prevMonth.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].minusMonths(1);
            render.run();
        });
        nextMonth.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].plusMonths(1);
            render.run();
        });
        prevYear.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].minusYears(1);
            render.run();
        });
        nextYear.addActionListener(e -> {
            currentMonth[0] = currentMonth[0].plusYears(1);
            render.run();
        });
        render.run();
        dialog.setVisible(true);
    }

    private JPanel buildWeekHeader() {
        JPanel header = new JPanel(new GridLayout(1, 7, 4, 4));
        for (int i = 1; i <= 7; i++) {
            java.time.DayOfWeek dow = java.time.DayOfWeek.of(i == 7 ? 7 : i); // keep order Mon..Sun
            JLabel lbl = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()), SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
            header.add(lbl);
        }
        return header;
    }

    private void setDateField(JTextField field, LocalDate date) {
        field.setText(date.format(displayFormatter));
    }

    private LocalDate parseDateField(JTextField field, String label) {
        try {
            return LocalDate.parse(field.getText().trim(), displayFormatter);
        } catch (Exception e) {
            if (label != null) {
                JOptionPane.showMessageDialog(this, label + " is not valid. Please pick a date.");
            }
            return null;
        }
    }
}
