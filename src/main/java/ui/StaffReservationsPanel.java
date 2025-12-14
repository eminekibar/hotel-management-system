package ui;

import model.reservation.Reservation;
import model.room.Room;
import model.user.Customer;
import model.user.Staff;
import service.CustomerService;
import service.ReservationService;
import service.RoomService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class StaffReservationsPanel extends JPanel {

    private final JFrame owner;
    private final Staff staff;
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final CustomerService customerService;
    private final Runnable roomsRefresher;

    private final DefaultListModel<String> reservationListModel = new DefaultListModel<>();
    private List<Reservation> cachedReservations;
    private final JTextField reservationCustomerFilter = new JTextField();
    private final JTextField reservationRoomFilter = new JTextField();
    private final JTextField reservationStartFilter = new JTextField();
    private final JTextField reservationEndFilter = new JTextField();
    private final DateTimeFormatter filterFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JTextField resStartDateField = new JTextField("2024-12-01");
    private final JTextField resEndDateField = new JTextField("2024-12-05");
    private final JComboBox<String> resRoomTypeBox = new JComboBox<>(new String[]{"standard", "suite", "family"});
    private final JSpinner resCapacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
    private final DefaultListModel<String> resSearchRoomsModel = new DefaultListModel<>();
    private List<model.room.RoomAvailabilityInfo> resSearchRooms;
    private final JList<String> reservationList = new JList<>(reservationListModel);
    private final JTextField resCustomerIdentifierField = new JTextField();
    private final JLabel selectedCustomerLabel = new JLabel("No customer selected");
    private Customer selectedCustomerForReservation;
    private final JTabbedPane tabs = new JTabbedPane();

    public StaffReservationsPanel(JFrame owner,
                                  Staff staff,
                                  ReservationService reservationService,
                                  RoomService roomService,
                                  CustomerService customerService,
                                  Runnable roomsRefresher) {
        this.owner = owner;
        this.staff = staff;
        this.reservationService = reservationService;
        this.roomService = roomService;
        this.customerService = customerService;
        this.roomsRefresher = roomsRefresher;
        buildUi();
    }

    private void buildUi() {
        tabs.addTab("List & Manage", manageReservationsPanel());
        tabs.addTab("Create New", createReservationPanel());
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel manageReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        reservationList.setCellRenderer(StaffListRenderers.createStaffReservationRenderer());
        reservationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(reservationList), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton checkIn = new JButton("Check In");
        checkIn.setToolTipText("Mark guest as checked-in");
        checkIn.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "checkin"));
        JButton checkOut = new JButton("Check Out");
        checkOut.setToolTipText("Mark guest as checked-out");
        checkOut.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "checkout"));
        JButton cancel = new JButton("Cancel Booking");
        cancel.setToolTipText("Cancel the reservation");
        cancel.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "cancel"));
        JButton markPaid = new JButton("Set Paid");
        markPaid.setToolTipText("Flag payment as received");
        markPaid.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "markPaid"));
        JButton refund = new JButton("Issue Refund");
        refund.setToolTipText("Record a refund for this booking");
        refund.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "refund"));
        JButton refresh = new JButton("Refresh List");
        refresh.setToolTipText("Reload reservations");
        refresh.addActionListener(e -> refreshReservations());
        buttons.add(checkIn);
        buttons.add(checkOut);
        buttons.add(cancel);
        buttons.add(markPaid);
        buttons.add(refund);
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);

        JPanel filterPanel = new JPanel(new GridLayout(0, 5, 4, 4));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Reservations"));
        filterPanel.add(new JLabel("Customer"));
        filterPanel.add(reservationCustomerFilter);
        filterPanel.add(new JLabel("Room"));
        filterPanel.add(reservationRoomFilter);
        filterPanel.add(new JLabel(""));
        filterPanel.add(new JLabel("Start Date"));
        filterPanel.add(wrapWithDatePicker(reservationStartFilter));
        filterPanel.add(new JLabel("End Date"));
        filterPanel.add(wrapWithDatePicker(reservationEndFilter));
        JButton applyFilter = new JButton("Apply");
        applyFilter.addActionListener(e -> refreshReservations());
        filterPanel.add(applyFilter);
        panel.add(filterPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createReservationPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBorder(BorderFactory.createTitledBorder("Create Reservation"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        form.add(new JLabel("Customer (username/email/TC)"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(resCustomerIdentifierField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel("Selected"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(selectedCustomerLabel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JButton loadCustomerBtn = new JButton("Load Customer");
        loadCustomerBtn.addActionListener(e -> loadCustomerForReservation());
        gbc.gridwidth = 2;
        form.add(loadCustomerBtn, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Start Date"), gbc);
        gbc.gridx = 1;
        form.add(resStartDateField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("End Date"), gbc);
        gbc.gridx = 1;
        form.add(resEndDateField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Room Type"), gbc);
        gbc.gridx = 1;
        form.add(resRoomTypeBox, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        form.add(new JLabel("Capacity"), gbc);
        gbc.gridx = 1;
        form.add(resCapacitySpinner, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton searchRoomsBtn = new JButton("Search Rooms");
        searchRoomsBtn.addActionListener(e -> searchRoomsForReservation());
        form.add(searchRoomsBtn, gbc);
        gbc.gridwidth = 1;

        JList<String> searchRoomsList = new JList<>(resSearchRoomsModel);
        searchRoomsList.setVisibleRowCount(6);
        JScrollPane roomsScroll = new JScrollPane(searchRoomsList);
        roomsScroll.setBorder(BorderFactory.createTitledBorder("Available Rooms"));

        JButton createReservationBtn = new JButton("Create Reservation");
        createReservationBtn.addActionListener(e -> createReservation(searchRoomsList.getSelectedIndex()));

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.add(roomsScroll, BorderLayout.CENTER);
        bottom.add(createReservationBtn, BorderLayout.SOUTH);

        wrapper.add(form, BorderLayout.NORTH);
        wrapper.add(bottom, BorderLayout.CENTER);
        return wrapper;
    }

    public void refreshReservations() {
        java.time.LocalDate start = null;
        java.time.LocalDate end = null;
        try {
            if (!reservationStartFilter.getText().trim().isBlank()) {
                start = java.time.LocalDate.parse(reservationStartFilter.getText().trim());
            }
            if (!reservationEndFilter.getText().trim().isBlank()) {
                end = java.time.LocalDate.parse(reservationEndFilter.getText().trim());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner, "Invalid date filter format. Use yyyy-MM-dd");
            return;
        }
        cachedReservations = reservationService.listReservationsByFilter(
                reservationCustomerFilter.getText().trim(),
                reservationRoomFilter.getText().trim(),
                start,
                end
        );
        reservationListModel.clear();
        for (Reservation res : cachedReservations) {
            reservationListModel.addElement(
                    res.getReservationId() + " | cust#" + res.getCustomer().getId() + " " + res.getCustomer().getDisplayName() +
                            " | room " + res.getRoom().getRoomNumber() + " (" + res.getRoom().getType() + ")" +
                            " | " + res.getStartDate() + " - " + res.getEndDate() +
                            " | status: " + res.getCurrentState().getName() +
                            " | payment: " + res.getPaymentStatus()
            );
        }
    }

    private void performOnSelected(int index, String action) {
        if (index < 0 || cachedReservations == null) {
            return;
        }
        Reservation reservation = cachedReservations.get(index);
        try {
            switch (action) {
                case "checkin" -> reservationService.checkIn(reservation.getReservationId(), staff.getId());
                case "checkout" -> reservationService.checkOut(reservation.getReservationId(), staff.getId());
                case "cancel" -> reservationService.cancelReservation(reservation.getReservationId(), staff.getId());
                case "markPaid" -> reservationService.markPaid(reservation.getReservationId(), staff.getId());
                case "refund" -> reservationService.refund(reservation.getReservationId(), staff.getId());
                default -> {
                }
            }
            refreshReservations();
            if (roomsRefresher != null) {
                roomsRefresher.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Action failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void searchRoomsForReservation() {
        try {
            String type = (String) resRoomTypeBox.getSelectedItem();
            int capacity = (Integer) resCapacitySpinner.getValue();
            LocalDate start = LocalDate.parse(resStartDateField.getText().trim());
            LocalDate end = LocalDate.parse(resEndDateField.getText().trim());
            resSearchRooms = roomService.searchWithAvailability(type, capacity, start, end);
            resSearchRoomsModel.clear();
            for (model.room.RoomAvailabilityInfo info : resSearchRooms) {
                Room room = info.getRoom();
                resSearchRoomsModel.addElement(room.getRoomNumber() + " | " + room.getType() + " | cap " + room.getCapacity() + " | $" + room.getPricePerNight() + " | " + info.getAvailabilityLabel());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner, "Invalid room search data: " + e.getMessage());
        }
    }

    private void createReservation(int selectedRoomIndex) {
        if (selectedRoomIndex < 0 || resSearchRooms == null || resSearchRooms.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "Select a room first");
            return;
        }
        if (selectedCustomerForReservation == null) {
            JOptionPane.showMessageDialog(owner, "Load a customer first");
            return;
        }
        try {
            model.room.RoomAvailabilityInfo info = resSearchRooms.get(selectedRoomIndex);
            if (!info.isBookable()) {
                JOptionPane.showMessageDialog(owner, "Room is not available for these dates.");
                return;
            }
            Room room = info.getRoom();
            LocalDate start = LocalDate.parse(resStartDateField.getText().trim());
            LocalDate end = LocalDate.parse(resEndDateField.getText().trim());
            Reservation reservation = reservationService.createReservation(selectedCustomerForReservation, room, start, end, staff.getId());
            JOptionPane.showMessageDialog(owner, "Reservation created: " + reservation.getReservationId());
            refreshReservations();
            if (roomsRefresher != null) {
                roomsRefresher.run();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(owner, "Failed to create reservation: " + e.getMessage());
        }
    }

    private JPanel wrapWithDatePicker(JTextField field) {
        field.setColumns(9);
        JButton button = new JButton("\uD83D\uDCC5");
        button.setMargin(new Insets(2, 6, 2, 6));
        button.setToolTipText("Pick a date");
        button.addActionListener(e -> openCalendar(field));
        JPanel wrapper = new JPanel(new BorderLayout(4, 0));
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(button, BorderLayout.EAST);
        return wrapper;
    }

    private void openCalendar(JTextField targetField) {
        LocalDate baseDate = parseFilterDate(targetField);
        if (baseDate == null) {
            baseDate = LocalDate.now();
        }
        JDialog dialog = new JDialog(owner, "Select date", true);
        dialog.setLayout(new BorderLayout(8, 8));
        JPanel monthNav = new JPanel(new BorderLayout());
        JButton prevMonth = new JButton("<");
        JButton nextMonth = new JButton(">");
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 13f));
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        navButtons.add(prevMonth);
        navButtons.add(monthLabel);
        navButtons.add(nextMonth);
        monthNav.add(navButtons, BorderLayout.CENTER);

        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        JPanel container = new JPanel(new BorderLayout(0, 6));
        container.add(buildWeekHeader(), BorderLayout.NORTH);
        container.add(daysPanel, BorderLayout.CENTER);

        dialog.add(monthNav, BorderLayout.NORTH);
        dialog.add(container, BorderLayout.CENTER);
        dialog.setSize(320, 260);
        dialog.setLocationRelativeTo(owner);

        YearMonth[] currentMonth = {YearMonth.from(baseDate)};
        Runnable render = () -> {
            String monthText = currentMonth[0].getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth[0].getYear();
            monthLabel.setText(monthText);
            daysPanel.removeAll();
            LocalDate firstOfMonth = currentMonth[0].atDay(1);
            int startOffset = (firstOfMonth.getDayOfWeek().getValue() + 6) % 7;
            for (int i = 0; i < startOffset; i++) {
                daysPanel.add(new JLabel(""));
            }
            int length = currentMonth[0].lengthOfMonth();
            for (int d = 1; d <= length; d++) {
                LocalDate date = currentMonth[0].atDay(d);
                JButton dayBtn = new JButton(String.valueOf(d));
                dayBtn.setMargin(new Insets(2, 2, 2, 2));
                dayBtn.addActionListener(e -> {
                    setFilterDate(targetField, date);
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
        render.run();
        dialog.setVisible(true);
    }

    private JPanel buildWeekHeader() {
        JPanel header = new JPanel(new GridLayout(1, 7, 4, 4));
        for (int i = 1; i <= 7; i++) {
            java.time.DayOfWeek dow = java.time.DayOfWeek.of(i == 7 ? 7 : i);
            JLabel lbl = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()), SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
            header.add(lbl);
        }
        return header;
    }

    private void setFilterDate(JTextField field, LocalDate date) {
        field.setText(date.format(filterFormatter));
    }

    private LocalDate parseFilterDate(JTextField field) {
        try {
            String text = field.getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            return LocalDate.parse(text, filterFormatter);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadCustomerForReservation() {
        String identifier = resCustomerIdentifierField.getText().trim();
        if (identifier.isBlank()) {
            JOptionPane.showMessageDialog(owner, "Enter username/email/TC to load customer.");
            return;
        }
        try {
            Customer customer = customerService.findByIdentifier(identifier);
            if (customer == null) {
                selectedCustomerForReservation = null;
                selectedCustomerLabel.setText("Not found");
                JOptionPane.showMessageDialog(owner, "Customer not found");
                return;
            }
            selectedCustomerForReservation = customer;
            selectedCustomerLabel.setText(customer.getId() + " | " + customer.getDisplayName() + " | " + customer.getUsername());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to load customer: " + ex.getMessage());
        }
    }
}
