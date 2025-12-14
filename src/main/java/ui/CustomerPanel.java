package ui;

import model.reservation.Reservation;
import model.Notification;
import model.room.RoomAvailabilityInfo;
import model.room.Room;
import model.user.Customer;
import service.CustomerService;
import service.NotificationQueryService;
import service.ReservationService;
import service.RoomService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class CustomerPanel extends JFrame {

    private final Customer customer;
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final ReservationService reservationService = new ReservationService();
    private final NotificationQueryService notificationQueryService = new NotificationQueryService();

    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JPasswordField newPasswordField = new JPasswordField();
    private final DefaultListModel<String> searchResultsModel = new DefaultListModel<>();
    private List<RoomAvailabilityInfo> lastAvailabilityResults;
    private final DefaultListModel<String> reservationListModel = new DefaultListModel<>();
    private final DefaultListModel<String> historyListModel = new DefaultListModel<>();

    private final JTextField startDateField = new JTextField();
    private final JTextField endDateField = new JTextField();
    private final JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"standard", "suite", "family"});
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
    private final JList<String> reservationList = new JList<>(reservationListModel);
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    private final DateTimeFormatter notificationFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DefaultListModel<String> notificationListModel = new DefaultListModel<>();
    private List<Notification> cachedNotifications;
    private final JList<String> notificationList = new JList<>(notificationListModel);

    public CustomerPanel(Customer customer) {
        this.customer = customer;
        setTitle("Customer Panel - " + customer.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);
        buildUi();
        refreshReservations();
        refreshHistory();
        refreshNotifications();
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
        tabs.addTab("Notifications", notificationsPanel());
        tabs.setSelectedIndex(0);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel profilePanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addInfoRow(infoPanel, gbc, row++, "Username:", new JLabel(customer.getUsername()));
        addInfoRow(infoPanel, gbc, row++, "First Name:", new JLabel(customer.getFirstName()));
        addInfoRow(infoPanel, gbc, row++, "Last Name:", new JLabel(customer.getLastName()));
        addInfoRow(infoPanel, gbc, row++, "National ID:", new JLabel(customer.getNationalId()));

        emailField.setText(customer.getEmail());
        addInfoRow(infoPanel, gbc, row++, "Email:", emailField);
        phoneField.setText(customer.getPhone());
        addInfoRow(infoPanel, gbc, row++, "Phone:", phoneField);

        container.add(infoPanel);

        JButton save = new JButton("Update Contact");
        save.addActionListener(e -> {
            customer.setEmail(emailField.getText().trim());
            customer.setPhone(phoneField.getText().trim());
            customerService.updateProfile(customer);
            JOptionPane.showMessageDialog(this, "Profile updated");
        });
        JPanel contactButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        contactButtons.add(save);
        container.add(contactButtons);

        JPanel passwordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(4, 6, 4, 6);
        pgbc.anchor = GridBagConstraints.LINE_START;
        pgbc.fill = GridBagConstraints.HORIZONTAL;
        pgbc.weightx = 0;
        pgbc.gridx = 0; pgbc.gridy = 0;
        passwordPanel.add(new JLabel("New Password:"), pgbc);
        pgbc.weightx = 1;
        pgbc.gridx = 1;
        passwordPanel.add(newPasswordField, pgbc);
        JButton changePassword = new JButton("Change Password");
        changePassword.addActionListener(e -> updatePassword());
        pgbc.weightx = 0;
        pgbc.gridx = 2;
        passwordPanel.add(changePassword, pgbc);
        container.add(passwordPanel);

        JButton deleteAccount = new JButton("Delete Account");
        deleteAccount.addActionListener(e -> deleteAccount());
        JPanel deleteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        deleteRow.add(deleteAccount);
        container.add(deleteRow);

        return container;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.weightx = 1;
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private JPanel searchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Check-in date:"));
        form.add(wrapWithDatePicker(startDateField, () -> LocalDate.now()));
        form.add(new JLabel("Check-out date:"));
        form.add(wrapWithDatePicker(endDateField, () -> {
            LocalDate start = parseDateField(startDateField, null);
            return start == null ? LocalDate.now().plusDays(1) : start.plusDays(1);
        }));
        form.add(new JLabel("Room type:"));
        form.add(roomTypeBox);
        form.add(new JLabel("Guests:"));
        form.add(capacitySpinner);
        JButton searchButton = new JButton("Find Rooms");
        searchButton.addActionListener(e -> searchRooms());
        form.add(searchButton);
        panel.add(form, BorderLayout.NORTH);

        JList<String> resultList = new JList<>(searchResultsModel);
        resultList.setCellRenderer(createRoomResultRenderer());
        panel.add(new JScrollPane(resultList), BorderLayout.CENTER);

        JButton reserveButton = new JButton("Book Selected");
        reserveButton.addActionListener(e -> reserveSelected(resultList.getSelectedIndex()));
        panel.add(reserveButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel reservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationList.setCellRenderer(createReservationRenderer());
        panel.add(new JScrollPane(reservationList), BorderLayout.CENTER);
        JButton cancel = new JButton("Cancel Selected");
        cancel.addActionListener(e -> cancelSelectedReservation());
        panel.add(cancel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel historyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> historyList = new JList<>(historyListModel);
        historyList.setCellRenderer(createReservationRenderer());
        panel.add(new JScrollPane(historyList), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshHistory());
        panel.add(refresh, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshNotifications() {
        try {
            cachedNotifications = notificationQueryService.listForCustomer(customer.getId());
            notificationListModel.clear();
            for (Notification n : cachedNotifications) {
                notificationListModel.addElement(formatNotification(n));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load notifications: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markNotificationRead(int index) {
        if (index < 0 || cachedNotifications == null || index >= cachedNotifications.size()) {
            JOptionPane.showMessageDialog(this, "Select a notification first");
            return;
        }
        Notification notification = cachedNotifications.get(index);
        try {
            notificationQueryService.markAsRead(notification.getId());
            refreshNotifications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to mark as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markAllNotificationsRead() {
        try {
            notificationQueryService.markAllAsReadForCustomer(customer.getId());
            refreshNotifications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to mark all as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel notificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        notificationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationList.setCellRenderer(createNotificationRenderer());
        panel.add(new JScrollPane(notificationList), BorderLayout.CENTER);

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
        panel.add(actions, BorderLayout.SOUTH);
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
            lastAvailabilityResults = availabilityList;
            searchResultsModel.clear();
            for (int i = 0; i < availabilityList.size(); i++) {
                var info = availabilityList.get(i);
                Room room = info.getRoom();
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
        if (index < 0 || lastAvailabilityResults == null || lastAvailabilityResults.isEmpty() || index >= lastAvailabilityResults.size()) {
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
            RoomAvailabilityInfo info = lastAvailabilityResults.get(index);
            if (!info.isBookable()) {
                JOptionPane.showMessageDialog(this, "Selected room is not available for these dates.");
                return;
            }
            Room room = info.getRoom();
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

    private JPanel wrapWithDatePicker(JTextField field, Supplier<LocalDate> minDateSupplier) {
        field.setEditable(false);
        field.setColumns(12);
        field.setHorizontalAlignment(JTextField.CENTER);
        JButton button = new JButton("\uD83D\uDCC5");
        button.setMargin(new Insets(2, 6, 2, 6));
        button.addActionListener(e -> openCalendar(field, minDateSupplier == null ? null : minDateSupplier.get()));
        JPanel wrapper = new JPanel(new BorderLayout(4, 0));
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(button, BorderLayout.EAST);
        return wrapper;
    }

    private void openCalendar(JTextField targetField, LocalDate minDate) {
        LocalDate baseDate = parseDateField(targetField, null);
        LocalDate today = LocalDate.now();
        if (baseDate == null) {
            baseDate = minDate != null ? minDate : today;
        }
        if (minDate != null && baseDate.isBefore(minDate)) {
            baseDate = minDate;
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
                boolean enabled = minDate == null || !date.isBefore(minDate);
                dayBtn.setEnabled(enabled);
                if (enabled) {
                    dayBtn.addActionListener(e -> {
                        setDateField(targetField, date);
                        dialog.dispose();
                    });
                }
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

    private ListCellRenderer<String> createRoomResultRenderer() {
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

    private ListCellRenderer<String> createReservationRenderer() {
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

    private ListCellRenderer<String> createNotificationRenderer() {
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

    private String part(String[] parts, int index) {
        return index < parts.length ? parts[index].trim() : "";
    }

    private String formatNotification(Notification n) {
        String time = n.getCreatedAt() == null ? "-" : LocalDateTime.ofInstant(n.getCreatedAt(), ZoneId.systemDefault()).format(notificationFormatter);
        return n.getId() + " | " + time + " | " + safe(n.getMessage()) + " | read:" + (n.isRead() ? "Y" : "N");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
