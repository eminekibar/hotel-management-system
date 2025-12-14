package ui;

import model.reservation.Reservation;
import model.room.Room;
import model.user.Customer;
import model.user.Staff;
import service.CustomerService;
import service.ReservationService;
import service.RoomService;
import service.StaffService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class StaffPanel extends JFrame {

    private final Staff staff;
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final ReservationService reservationService = new ReservationService();
    private final StaffService staffService = new StaffService();

    private final DefaultListModel<String> customerListModel = new DefaultListModel<>();
    private List<Customer> cachedCustomers;
    private final JTextField customerSearchField = new JTextField();

    private final DefaultListModel<String> staffListModel = new DefaultListModel<>();
    private List<Staff> cachedStaff;
    private final JTextField staffSearchField = new JTextField();
    private final JTextField staffUsernameField = new JTextField();
    private final JTextField staffFirstNameField = new JTextField();
    private final JTextField staffLastNameField = new JTextField();
    private final JTextField staffEmailField = new JTextField();
    private final JTextField staffNationalIdField = new JTextField();
    private final JPasswordField staffPasswordField = new JPasswordField();
    private final JComboBox<String> staffRoleBox = new JComboBox<>(new String[]{"staff", "admin"});

    private final DefaultListModel<String> roomListModel = new DefaultListModel<>();
    private List<Room> cachedRooms;

    private final DefaultListModel<String> reservationListModel = new DefaultListModel<>();
    private List<Reservation> cachedReservations;
    private final JTextField reservationCustomerFilter = new JTextField();
    private final JTextField reservationRoomFilter = new JTextField();
    private final JTextField reservationStartFilter = new JTextField();
    private final JTextField reservationEndFilter = new JTextField();

    private final JTextField roomNumberField = new JTextField();
    private final JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"standard", "suite", "family"});
    private final JSpinner roomCapacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
    private final JSpinner roomPriceSpinner = new JSpinner(new SpinnerNumberModel(100.0, 10.0, 5000.0, 10.0));

    private final JTextField resCustomerIdField = new JTextField();
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

    public StaffPanel(Staff staff) {
        this.staff = staff;
        setTitle("Staff Panel - " + staff.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        buildUi();
        refreshCustomers();
        if (isAdmin()) {
            refreshStaff();
        }
        refreshRooms();
        refreshReservations();
        setVisible(true);
    }

    private boolean isAdmin() {
        return staff.getUsername() != null && staff.getUsername().equalsIgnoreCase("admin");
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel("Logged in as: " + staff.getDisplayName()), BorderLayout.WEST);
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> logout());
        header.add(logout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customers", customersPanel());
        if (isAdmin()) {
            tabs.addTab("Staff", staffPanel());
        }
        tabs.addTab("Rooms", roomsPanel());
        tabs.addTab("Reservations", reservationsPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel customersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> list = new JList<>(customerListModel);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(customerSearchField, BorderLayout.CENTER);
        JButton search = new JButton("Search");
        search.addActionListener(e -> refreshCustomers());
        JButton addCustomer = new JButton("Add Customer");
        addCustomer.addActionListener(e -> openCustomerDialog());
        JButton details = new JButton("Details");
        details.addActionListener(e -> openCustomerDetails(list.getSelectedIndex()));
        JButton deactivate = new JButton("Deactivate");
        deactivate.addActionListener(e -> deactivateCustomer(list.getSelectedIndex()));
        JPanel buttons = new JPanel();
        buttons.add(search);
        buttons.add(addCustomer);
        buttons.add(details);
        buttons.add(deactivate);
        top.add(buttons, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshCustomers());
        panel.add(refresh, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel staffPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> list = new JList<>(staffListModel);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(staffSearchField, BorderLayout.CENTER);
        JButton search = new JButton("Search");
        search.addActionListener(e -> refreshStaff());
        JButton deactivate = new JButton("Deactivate");
        deactivate.addActionListener(e -> deactivateStaff(list.getSelectedIndex()));
        JPanel buttons = new JPanel();
        buttons.add(search);
        buttons.add(deactivate);
        top.add(buttons, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
        form.setBorder(BorderFactory.createTitledBorder("Add Staff"));
        form.add(new JLabel("Username"));
        form.add(staffUsernameField);
        form.add(new JLabel("First Name"));
        form.add(staffFirstNameField);
        form.add(new JLabel("Last Name"));
        form.add(staffLastNameField);
        form.add(new JLabel("Email"));
        form.add(staffEmailField);
        form.add(new JLabel("National ID"));
        form.add(staffNationalIdField);
        form.add(new JLabel("Password"));
        form.add(staffPasswordField);
        form.add(new JLabel("Role"));
        form.add(staffRoleBox);
        JButton addBtn = new JButton("Add Staff");
        addBtn.addActionListener(e -> addStaff());
        form.add(new JLabel(""));
        form.add(addBtn);
        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel roomsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> list = new JList<>(roomListModel);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 4, 4));
        form.add(new JLabel("Room Number"));
        form.add(roomNumberField);
        form.add(new JLabel("Type"));
        form.add(roomTypeBox);
        form.add(new JLabel("Capacity"));
        form.add(roomCapacitySpinner);
        form.add(new JLabel("Price/Night"));
        form.add(roomPriceSpinner);
        JButton addRoomButton = new JButton("Add Room");
        addRoomButton.addActionListener(e -> addRoom());
        form.add(addRoomButton);
        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel reservationsPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("List & Manage", manageReservationsPanel());
        tabs.addTab("Create New", createReservationPanel());
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel manageReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(reservationList), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton checkIn = new JButton("Check-in");
        checkIn.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "checkin"));
        JButton checkOut = new JButton("Check-out");
        checkOut.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "checkout"));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "cancel"));
        JButton markPaid = new JButton("Mark Paid");
        markPaid.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "markPaid"));
        JButton refund = new JButton("Refund");
        refund.addActionListener(e -> performOnSelected(reservationList.getSelectedIndex(), "refund"));
        JButton refresh = new JButton("Refresh");
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
        filterPanel.add(new JLabel("Start Date"));
        filterPanel.add(reservationStartFilter);
        filterPanel.add(new JLabel("End Date"));
        filterPanel.add(reservationEndFilter);
        JButton applyFilter = new JButton("Apply");
        applyFilter.addActionListener(e -> refreshReservations());
        filterPanel.add(applyFilter);
        panel.add(filterPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createReservationPanel() {
        JPanel createPanel = new JPanel(new GridLayout(0, 2, 4, 4));
        createPanel.setBorder(BorderFactory.createTitledBorder("Create Reservation"));
        createPanel.add(new JLabel("Customer (username/email/TC)"));
        createPanel.add(resCustomerIdentifierField);
        createPanel.add(new JLabel("Selected"));
        createPanel.add(selectedCustomerLabel);
        JButton loadCustomerBtn = new JButton("Load Customer");
        loadCustomerBtn.addActionListener(e -> loadCustomerForReservation());
        createPanel.add(loadCustomerBtn);
        createPanel.add(new JLabel(""));
        createPanel.add(new JLabel("Start Date"));
        createPanel.add(resStartDateField);
        createPanel.add(new JLabel("End Date"));
        createPanel.add(resEndDateField);
        createPanel.add(new JLabel("Room Type"));
        createPanel.add(resRoomTypeBox);
        createPanel.add(new JLabel("Capacity"));
        createPanel.add(resCapacitySpinner);
        JButton searchRoomsBtn = new JButton("Search Rooms");
        searchRoomsBtn.addActionListener(e -> searchRoomsForReservation());
        createPanel.add(searchRoomsBtn);
        JList<String> searchRoomsList = new JList<>(resSearchRoomsModel);
        createPanel.add(new JScrollPane(searchRoomsList));
        JButton createReservationBtn = new JButton("Create Reservation");
        createReservationBtn.addActionListener(e -> createReservation(searchRoomsList.getSelectedIndex()));
        createPanel.add(createReservationBtn);
        return createPanel;
    }

    private void refreshCustomers() {
        cachedCustomers = customerService.searchCustomers(customerSearchField.getText().trim());
        customerListModel.clear();
        for (Customer c : cachedCustomers) {
            customerListModel.addElement(c.getId() + " | " + c.getDisplayName() + " | " + c.getUsername() + " | " + c.getEmail() + " | active=" + (c.isActive() ? "Y" : "N"));
        }
    }

    private void deactivateCustomer(int index) {
        if (index < 0 || cachedCustomers == null || index >= cachedCustomers.size()) {
            JOptionPane.showMessageDialog(this, "Select a customer first");
            return;
        }
        Customer customer = cachedCustomers.get(index);
        if (!customer.isActive()) {
            JOptionPane.showMessageDialog(this, "Customer is already inactive.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate customer " + customer.getDisplayName() + "? Existing reservations stay active.",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            customerService.deactivate(customer.getId());
            JOptionPane.showMessageDialog(this, "Customer deactivated.");
            refreshCustomers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to deactivate customer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshStaff() {
        cachedStaff = staffService.searchStaff(staffSearchField.getText().trim());
        staffListModel.clear();
        for (Staff s : cachedStaff) {
            staffListModel.addElement(s.getId() + " | " + s.getDisplayName() + " | " + s.getUsername() + " | role=" + s.getRole() + " | active=" + (s.isActive() ? "Y" : "N"));
        }
    }

    private void addStaff() {
        String username = staffUsernameField.getText().trim();
        String firstName = staffFirstNameField.getText().trim();
        String lastName = staffLastNameField.getText().trim();
        String email = staffEmailField.getText().trim();
        String nationalId = staffNationalIdField.getText().trim();
        String password = new String(staffPasswordField.getPassword());
        String role = (String) staffRoleBox.getSelectedItem();

        try {
            Staff created = staffService.register(username, firstName, lastName, email, nationalId, role, password);
            JOptionPane.showMessageDialog(this, "Staff created: " + created.getUsername());
            refreshStaff();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deactivateStaff(int index) {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(this, "Only admin can deactivate staff.");
            return;
        }
        if (index < 0 || cachedStaff == null || index >= cachedStaff.size()) {
            JOptionPane.showMessageDialog(this, "Select a staff first");
            return;
        }
        Staff target = cachedStaff.get(index);
        if (target.getUsername() != null && target.getUsername().equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(this, "Admin account cannot be deactivated.");
            return;
        }
        if (!target.isActive()) {
            JOptionPane.showMessageDialog(this, "Staff is already inactive.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Deactivate staff " + target.getDisplayName() + "?",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            staffService.deactivate(target.getId());
            JOptionPane.showMessageDialog(this, "Staff deactivated.");
            refreshStaff();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to deactivate staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshRooms() {
        cachedRooms = roomService.listRooms();
        roomListModel.clear();
        for (Room room : cachedRooms) {
            roomListModel.addElement(room.getId() + " | " + room.getRoomNumber() + " | " + room.getType() + " | " + room.getStatus() + " | " + room.getPricePerNight());
        }
    }

    private void refreshReservations() {
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
            JOptionPane.showMessageDialog(this, "Invalid date filter format. Use yyyy-MM-dd");
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

    private void addRoom() {
        Room room = roomService.addRoom(
                roomNumberField.getText().trim(),
                (String) roomTypeBox.getSelectedItem(),
                (Integer) roomCapacitySpinner.getValue(),
                ((Double) roomPriceSpinner.getValue())
        );
        JOptionPane.showMessageDialog(this, "Room added: " + room.getRoomNumber());
        refreshRooms();
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
            refreshRooms();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Action failed", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Invalid room search data: " + e.getMessage());
        }
    }

    private void createReservation(int selectedRoomIndex) {
        if (selectedRoomIndex < 0 || resSearchRooms == null || resSearchRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a room first");
            return;
        }
        if (selectedCustomerForReservation == null) {
            JOptionPane.showMessageDialog(this, "Load a customer first");
            return;
        }
        try {
            model.room.RoomAvailabilityInfo info = resSearchRooms.get(selectedRoomIndex);
            if (!info.isBookable()) {
                JOptionPane.showMessageDialog(this, "Room is not available for these dates.");
                return;
            }
            Room room = info.getRoom();
            LocalDate start = LocalDate.parse(resStartDateField.getText().trim());
            LocalDate end = LocalDate.parse(resEndDateField.getText().trim());
            Reservation reservation = reservationService.createReservation(selectedCustomerForReservation, room, start, end);
            JOptionPane.showMessageDialog(this, "Reservation created: " + reservation.getReservationId());
            refreshReservations();
            refreshRooms();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to create reservation: " + e.getMessage());
        }
    }

    private void openCustomerDialog() {
        CustomerRegistrationDialog dialog = new CustomerRegistrationDialog(this, customerService);
        dialog.setVisible(true);
        refreshCustomers();
    }

    private void openCustomerDetails(int index) {
        if (index < 0 || cachedCustomers == null || index >= cachedCustomers.size()) {
            JOptionPane.showMessageDialog(this, "Select a customer first");
            return;
        }
        Customer customer = cachedCustomers.get(index);
        CustomerDetailDialog dialog = new CustomerDetailDialog(this, customer, reservationService);
        dialog.setVisible(true);
    }

    private void logout() {
        dispose();
        new LoginForm();
    }

    private void loadCustomerForReservation() {
        String identifier = resCustomerIdentifierField.getText().trim();
        if (identifier.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter username/email/TC to load customer.");
            return;
        }
        try {
            Customer customer = customerService.findByIdentifier(identifier);
            if (customer == null) {
                selectedCustomerForReservation = null;
                selectedCustomerLabel.setText("Not found");
                JOptionPane.showMessageDialog(this, "Customer not found");
                return;
            }
            selectedCustomerForReservation = customer;
            selectedCustomerLabel.setText(customer.getId() + " | " + customer.getDisplayName() + " | " + customer.getUsername());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load customer: " + ex.getMessage());
        }
    }
}
