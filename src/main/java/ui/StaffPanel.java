package ui;

import model.reservation.Reservation;
import model.room.Room;
import model.Notification;
import model.user.Customer;
import model.user.Staff;
import service.CustomerService;
import service.NotificationQueryService;
import service.ReservationService;
import service.RoomService;
import service.StaffService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StaffPanel extends JFrame {

    private final Staff staff;
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final ReservationService reservationService = new ReservationService();
    private final StaffService staffService = new StaffService();
    private final NotificationQueryService notificationQueryService = new NotificationQueryService();

    private final DefaultListModel<String> customerListModel = new DefaultListModel<>();
    private List<Customer> cachedCustomers;
    private List<Customer> visibleCustomers;
    private final JTextField customerSearchField = new JTextField();
    private final JComboBox<String> customerStatusFilter = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
    private final JTextField customerUsernameField = new JTextField();
    private final JTextField customerFirstNameField = new JTextField();
    private final JTextField customerLastNameField = new JTextField();
    private final JTextField customerEmailField = new JTextField();
    private final JTextField customerPhoneField = new JTextField();
    private final JTextField customerNationalIdField = new JTextField();
    private final JPasswordField customerPasswordField = new JPasswordField();

    private final DefaultListModel<String> staffListModel = new DefaultListModel<>();
    private List<Staff> cachedStaff;
    private List<Staff> visibleStaff;
    private final JTextField staffSearchField = new JTextField();
    private final JTextField staffUsernameField = new JTextField();
    private final JTextField staffFirstNameField = new JTextField();
    private final JTextField staffLastNameField = new JTextField();
    private final JTextField staffEmailField = new JTextField();
    private final JTextField staffNationalIdField = new JTextField();
    private final JPasswordField staffPasswordField = new JPasswordField();
    private final JComboBox<String> staffRoleBox = new JComboBox<>(new String[]{"staff", "admin"});
    private final JComboBox<String> staffStatusFilter = new JComboBox<>(new String[]{"All", "Active", "Inactive"});
    private final JComboBox<String> staffRoleFilter = new JComboBox<>(new String[]{"All", "staff", "admin"});

    private final DefaultListModel<String> roomListModel = new DefaultListModel<>();
    private List<Room> cachedRooms;
    private List<Room> filteredRooms;
    private final JTextField roomSearchField = new JTextField();
    private final JComboBox<String> roomTypeFilter = new JComboBox<>(new String[]{"All", "standard", "suite", "family"});
    private final JComboBox<String> roomStatusFilter = new JComboBox<>(new String[]{"All", "available", "reserved", "maintenance", "inactive"});
    private final JSpinner roomCapacityFilter = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));

    private final DefaultListModel<String> reservationListModel = new DefaultListModel<>();
    private List<Reservation> cachedReservations;
    private final JTextField reservationCustomerFilter = new JTextField();
    private final JTextField reservationRoomFilter = new JTextField();
    private final JTextField reservationStartFilter = new JTextField();
    private final JTextField reservationEndFilter = new JTextField();
    private final DateTimeFormatter filterFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter notificationTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    private final DefaultListModel<String> notificationListModel = new DefaultListModel<>();
    private List<Notification> cachedNotifications;
    private final JList<String> notificationList = new JList<>(notificationListModel);
    private List<List<Notification>> adminGroupedNotifications;

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
        refreshNotifications();
        setVisible(true);
    }

    private boolean isAdmin() {
        return staff.getUsername() != null && staff.getUsername().equalsIgnoreCase("admin");
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        String roleLabel = (staff.getRole() == null || staff.getRole().isBlank()) ? "Staff" : staff.getRole();
        String displayName = staff.getDisplayName() == null ? "" : staff.getDisplayName();
        boolean roleAlreadyInName = displayName.toLowerCase().contains(roleLabel.toLowerCase());
        String welcomeText = "Welcome, " + displayName + (roleAlreadyInName ? "" : " (" + roleLabel + ")");
        JLabel welcome = new JLabel(welcomeText);
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 16f));
        welcome.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        header.add(welcome, BorderLayout.WEST);
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
        tabs.addTab("Notifications", notificationsPanel());
        int reservationsTabIndex = tabs.indexOfTab("Reservations");
        if (reservationsTabIndex >= 0) {
            tabs.setSelectedIndex(reservationsTabIndex);
        }
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel customersPanel() {
        JTabbedPane innerTabs = new JTabbedPane();
        innerTabs.addTab("List & Manage", manageCustomersPanel(innerTabs));
        innerTabs.addTab("Create New", createCustomerPanel(innerTabs));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(innerTabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel manageCustomersPanel(JTabbedPane parentTabs) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JList<String> list = new JList<>(customerListModel);
        list.setCellRenderer(createCustomerRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel filterFields = new JPanel(new GridLayout(2, 3, 6, 6));
        filterFields.add(new JLabel("Search (name, username, email)"));
        filterFields.add(customerSearchField);
        filterFields.add(new JLabel(""));
        filterFields.add(new JLabel("Status"));
        filterFields.add(customerStatusFilter);
        JPanel filterButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> refreshCustomers());
        JButton clear = new JButton("Reset");
        clear.addActionListener(e -> {
            customerSearchField.setText("");
            customerStatusFilter.setSelectedIndex(0);
            refreshCustomers();
        });
        filterButtons.add(apply);
        filterButtons.add(clear);
        filterFields.add(filterButtons);

        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setBorder(BorderFactory.createTitledBorder("Filter Customers"));
        filterWrapper.add(filterFields, BorderLayout.CENTER);
        panel.add(filterWrapper, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton details = new JButton("Details");
        details.addActionListener(e -> openCustomerDetails(list.getSelectedIndex()));
        JButton deactivate = new JButton("Deactivate");
        deactivate.addActionListener(e -> deactivateCustomer(list.getSelectedIndex()));
        JButton refresh = new JButton("Refresh List");
        refresh.addActionListener(e -> refreshCustomers());
        JButton toCreate = new JButton("Create New");
        toCreate.addActionListener(e -> parentTabs.setSelectedIndex(1));
        actions.add(details);
        actions.add(deactivate);
        actions.add(refresh);
        actions.add(toCreate);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCustomerPanel(JTabbedPane parentTabs) {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Create Customer"));
        form.add(new JLabel("Username"));
        form.add(customerUsernameField);
        form.add(new JLabel("First Name"));
        form.add(customerFirstNameField);
        form.add(new JLabel("Last Name"));
        form.add(customerLastNameField);
        form.add(new JLabel("Email"));
        form.add(customerEmailField);
        form.add(new JLabel("Phone"));
        form.add(customerPhoneField);
        form.add(new JLabel("National ID (11 digits)"));
        form.add(customerNationalIdField);
        form.add(new JLabel("Password"));
        form.add(customerPasswordField);
        JButton create = new JButton("Add Customer");
        create.addActionListener(e -> registerCustomer(parentTabs));
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> clearCustomerCreateForm());
        form.add(create);
        form.add(clear);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel staffPanel() {
        JTabbedPane innerTabs = new JTabbedPane();
        innerTabs.addTab("List & Manage", manageStaffPanel(innerTabs));
        innerTabs.addTab("Create New", createStaffPanel(innerTabs));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(innerTabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel manageStaffPanel(JTabbedPane parentTabs) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JList<String> list = new JList<>(staffListModel);
        list.setCellRenderer(createStaffRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel filterFields = new JPanel(new GridLayout(2, 3, 6, 6));
        filterFields.add(new JLabel("Search (name/username/email)"));
        filterFields.add(staffSearchField);
        filterFields.add(new JLabel(""));
        filterFields.add(new JLabel("Status"));
        filterFields.add(staffStatusFilter);
        JPanel extra = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        extra.add(new JLabel("Role"));
        extra.add(staffRoleFilter);
        filterFields.add(extra);

        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setBorder(BorderFactory.createTitledBorder("Filter Staff"));
        filterWrapper.add(filterFields, BorderLayout.CENTER);
        JPanel filterButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> refreshStaff());
        JButton clear = new JButton("Reset");
        clear.addActionListener(e -> {
            staffSearchField.setText("");
            staffStatusFilter.setSelectedIndex(0);
            staffRoleFilter.setSelectedIndex(0);
            refreshStaff();
        });
        filterButtons.add(apply);
        filterButtons.add(clear);
        filterWrapper.add(filterButtons, BorderLayout.EAST);
        panel.add(filterWrapper, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton deactivate = new JButton("Deactivate");
        deactivate.addActionListener(e -> deactivateStaff(list.getSelectedIndex()));
        JButton refresh = new JButton("Refresh List");
        refresh.addActionListener(e -> refreshStaff());
        JButton toCreate = new JButton("Create New");
        toCreate.addActionListener(e -> parentTabs.setSelectedIndex(1));
        actions.add(deactivate);
        actions.add(refresh);
        actions.add(toCreate);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStaffPanel(JTabbedPane parentTabs) {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Create Staff"));
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
        addBtn.addActionListener(e -> registerStaff(parentTabs));
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> clearStaffCreateForm());
        form.add(addBtn);
        form.add(clear);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel roomsPanel() {
        JTabbedPane innerTabs = new JTabbedPane();
        innerTabs.addTab("List & Manage", manageRoomsPanel(innerTabs));
        innerTabs.addTab("Create New", createRoomPanel(innerTabs));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(innerTabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel manageRoomsPanel(JTabbedPane parentTabs) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JList<String> list = new JList<>(roomListModel);
        list.setCellRenderer(createRoomRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel filterFields = new JPanel(new GridLayout(2, 4, 6, 6));
        filterFields.add(new JLabel("Search (room no / type)"));
        filterFields.add(roomSearchField);
        filterFields.add(new JLabel("Type"));
        filterFields.add(roomTypeFilter);
        filterFields.add(new JLabel("Status"));
        filterFields.add(roomStatusFilter);
        filterFields.add(new JLabel("Min Capacity"));
        filterFields.add(roomCapacityFilter);

        JPanel filterWrapper = new JPanel(new BorderLayout());
        filterWrapper.setBorder(BorderFactory.createTitledBorder("Filter Rooms"));
        filterWrapper.add(filterFields, BorderLayout.CENTER);
        JPanel filterButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> refreshRooms());
        JButton clear = new JButton("Reset");
        clear.addActionListener(e -> {
            roomSearchField.setText("");
            roomTypeFilter.setSelectedIndex(0);
            roomStatusFilter.setSelectedIndex(0);
            roomCapacityFilter.setValue(0);
            refreshRooms();
        });
        filterButtons.add(apply);
        filterButtons.add(clear);
        filterWrapper.add(filterButtons, BorderLayout.EAST);
        panel.add(filterWrapper, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton setAvailable = new JButton("Set Available");
        setAvailable.addActionListener(e -> updateRoomStatus(list.getSelectedIndex(), "available"));
        JButton setMaintenance = new JButton("Set Maintenance");
        setMaintenance.addActionListener(e -> updateRoomStatus(list.getSelectedIndex(), "maintenance"));
        JButton setInactive = new JButton("Set Inactive");
        setInactive.addActionListener(e -> updateRoomStatus(list.getSelectedIndex(), "inactive"));
        JButton refresh = new JButton("Refresh List");
        refresh.addActionListener(e -> refreshRooms());
        JButton toCreate = new JButton("Create New");
        toCreate.addActionListener(e -> parentTabs.setSelectedIndex(1));
        actions.add(setAvailable);
        actions.add(setMaintenance);
        actions.add(setInactive);
        actions.add(refresh);
        actions.add(toCreate);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRoomPanel(JTabbedPane parentTabs) {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Create Room"));
        form.add(new JLabel("Room Number"));
        form.add(roomNumberField);
        form.add(new JLabel("Type"));
        form.add(roomTypeBox);
        form.add(new JLabel("Capacity"));
        form.add(roomCapacitySpinner);
        form.add(new JLabel("Price/Night"));
        form.add(roomPriceSpinner);
        JButton addRoomButton = new JButton("Add Room");
        addRoomButton.addActionListener(e -> {
            addRoom();
            parentTabs.setSelectedIndex(0);
        });
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> clearRoomCreateForm());
        form.add(addRoomButton);
        form.add(clear);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel reservationsPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("List & Manage", manageReservationsPanel());
        tabs.addTab("Create New", createReservationPanel());
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
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

    private JPanel manageReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        reservationList.setCellRenderer(createStaffReservationRenderer());
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

    private void refreshCustomers() {
        cachedCustomers = customerService.searchCustomers(customerSearchField.getText().trim());
        applyCustomerFilters();
    }

    private void applyCustomerFilters() {
        customerListModel.clear();
        visibleCustomers = new ArrayList<>();
        if (cachedCustomers == null) {
            return;
        }
        String status = (String) customerStatusFilter.getSelectedItem();
        for (Customer c : cachedCustomers) {
            if ("Active".equals(status) && !c.isActive()) {
                continue;
            }
            if ("Inactive".equals(status) && c.isActive()) {
                continue;
            }
            visibleCustomers.add(c);
            customerListModel.addElement(formatCustomer(c));
        }
    }

    private void refreshNotifications() {
        try {
            if (isAdmin()) {
                cachedNotifications = notificationQueryService.listAll();
                renderAdminNotifications(cachedNotifications);
            } else {
                cachedNotifications = notificationQueryService.listForStaff(staff.getId());
                adminGroupedNotifications = null;
                notificationListModel.clear();
                for (Notification n : cachedNotifications) {
                    notificationListModel.addElement(formatNotification(n));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load notifications: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerCustomer(JTabbedPane parentTabs) {
        try {
            Customer created = customerService.register(
                    customerUsernameField.getText().trim(),
                    customerFirstNameField.getText().trim(),
                    customerLastNameField.getText().trim(),
                    customerEmailField.getText().trim(),
                    customerPhoneField.getText().trim(),
                    customerNationalIdField.getText().trim(),
                    new String(customerPasswordField.getPassword())
            );
            JOptionPane.showMessageDialog(this, "Customer created: " + created.getUsername());
            clearCustomerCreateForm();
            refreshCustomers();
            if (parentTabs != null) {
                parentTabs.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create customer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearCustomerCreateForm() {
        customerUsernameField.setText("");
        customerFirstNameField.setText("");
        customerLastNameField.setText("");
        customerEmailField.setText("");
        customerPhoneField.setText("");
        customerNationalIdField.setText("");
        customerPasswordField.setText("");
    }

    private void deactivateCustomer(int index) {
        if (index < 0 || visibleCustomers == null || index >= visibleCustomers.size()) {
            JOptionPane.showMessageDialog(this, "Select a customer first");
            return;
        }
        Customer customer = visibleCustomers.get(index);
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
        applyStaffFilters();
    }

    private void applyStaffFilters() {
        staffListModel.clear();
        visibleStaff = new ArrayList<>();
        if (cachedStaff == null) {
            return;
        }
        String status = (String) staffStatusFilter.getSelectedItem();
        String roleFilter = (String) staffRoleFilter.getSelectedItem();
        for (Staff s : cachedStaff) {
            if ("Active".equals(status) && !s.isActive()) {
                continue;
            }
            if ("Inactive".equals(status) && s.isActive()) {
                continue;
            }
            if (!"All".equalsIgnoreCase(roleFilter)) {
                String staffRole = s.getRole() == null ? "" : s.getRole();
                if (!staffRole.equalsIgnoreCase(roleFilter)) {
                    continue;
                }
            }
            visibleStaff.add(s);
            staffListModel.addElement(formatStaff(s));
        }
    }

    private void registerStaff(JTabbedPane parentTabs) {
        try {
            Staff created = staffService.register(
                    staffUsernameField.getText().trim(),
                    staffFirstNameField.getText().trim(),
                    staffLastNameField.getText().trim(),
                    staffEmailField.getText().trim(),
                    staffNationalIdField.getText().trim(),
                    (String) staffRoleBox.getSelectedItem(),
                    new String(staffPasswordField.getPassword())
            );
            JOptionPane.showMessageDialog(this, "Staff created: " + created.getUsername());
            clearStaffCreateForm();
            refreshStaff();
            if (parentTabs != null) {
                parentTabs.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearStaffCreateForm() {
        staffUsernameField.setText("");
        staffFirstNameField.setText("");
        staffLastNameField.setText("");
        staffEmailField.setText("");
        staffNationalIdField.setText("");
        staffPasswordField.setText("");
        staffRoleBox.setSelectedIndex(0);
    }

    private void deactivateStaff(int index) {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(this, "Only admin can deactivate staff.");
            return;
        }
        if (index < 0 || visibleStaff == null || index >= visibleStaff.size()) {
            JOptionPane.showMessageDialog(this, "Select a staff first");
            return;
        }
        Staff target = visibleStaff.get(index);
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
        applyRoomFilters();
    }

    private void markNotificationRead(int index) {
        if (isAdmin() && adminGroupedNotifications != null) {
            if (index < 0 || index >= adminGroupedNotifications.size()) {
                JOptionPane.showMessageDialog(this, "Select a notification first");
                return;
            }
            try {
                for (Notification n : adminGroupedNotifications.get(index)) {
                    notificationQueryService.markAsRead(n.getId());
                }
                refreshNotifications();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to mark as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
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
            if (isAdmin()) {
                notificationQueryService.markAllAsReadAll();
            } else {
                notificationQueryService.markAllAsReadForStaff(staff.getId());
            }
            refreshNotifications();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to mark all as read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyRoomFilters() {
        roomListModel.clear();
        filteredRooms = new ArrayList<>();
        if (cachedRooms == null) {
            return;
        }
        String search = roomSearchField.getText().trim().toLowerCase();
        String type = (String) roomTypeFilter.getSelectedItem();
        String status = (String) roomStatusFilter.getSelectedItem();
        int minCapacity = (Integer) roomCapacityFilter.getValue();
        for (Room room : cachedRooms) {
            if (!"All".equalsIgnoreCase(type)) {
                if (room.getType() == null || !room.getType().equalsIgnoreCase(type)) {
                    continue;
                }
            }
            if (!"All".equalsIgnoreCase(status)) {
                if (room.getStatus() == null || !room.getStatus().equalsIgnoreCase(status)) {
                    continue;
                }
            }
            if (minCapacity > 0 && room.getCapacity() < minCapacity) {
                continue;
            }
            String combined = (safe(room.getRoomNumber()) + " " + safe(room.getType())).toLowerCase();
            if (!search.isEmpty() && !combined.contains(search)) {
                continue;
            }
            filteredRooms.add(room);
            roomListModel.addElement(formatRoom(room));
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
        clearRoomCreateForm();
        refreshRooms();
    }

    private void updateRoomStatus(int selectedIndex, String status) {
        if (selectedIndex < 0 || filteredRooms == null || selectedIndex >= filteredRooms.size()) {
            JOptionPane.showMessageDialog(this, "Select a room first");
            return;
        }
        Room room = filteredRooms.get(selectedIndex);
        try {
            roomService.updateStatus(room.getId(), status);
            JOptionPane.showMessageDialog(this, "Room " + room.getRoomNumber() + " set to " + status);
            refreshRooms();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearRoomCreateForm() {
        roomNumberField.setText("");
        roomTypeBox.setSelectedIndex(0);
        roomCapacitySpinner.setValue(2);
        roomPriceSpinner.setValue(100.0);
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
            Reservation reservation = reservationService.createReservation(selectedCustomerForReservation, room, start, end, staff.getId());
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
        if (index < 0 || visibleCustomers == null || index >= visibleCustomers.size()) {
            JOptionPane.showMessageDialog(this, "Select a customer first");
            return;
        }
        Customer customer = visibleCustomers.get(index);
        CustomerDetailDialog dialog = new CustomerDetailDialog(this, customer, reservationService);
        dialog.setVisible(true);
    }

    private void logout() {
        dispose();
        new LoginForm();
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
        JDialog dialog = new JDialog(this, "Select date", true);
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
        dialog.setLocationRelativeTo(this);

        YearMonth[] currentMonth = {YearMonth.from(baseDate)};
        Runnable render = () -> {
            String monthText = currentMonth[0].getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth[0].getYear();
            monthLabel.setText(monthText);
            daysPanel.removeAll();
            LocalDate firstOfMonth = currentMonth[0].atDay(1);
            int startOffset = (firstOfMonth.getDayOfWeek().getValue() + 6) % 7; // Monday=0..Sunday=6
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

    private String formatCustomer(Customer c) {
        return c.getId() + " | " + c.getDisplayName() +
                " | user:" + safe(c.getUsername()) +
                " | email:" + safe(c.getEmail()) +
                " | phone:" + safe(c.getPhone()) +
                " | tc:" + safe(c.getNationalId()) +
                " | active:" + (c.isActive() ? "Y" : "N");
    }

    private String formatRoom(Room room) {
        return room.getId() + " | room " + safe(room.getRoomNumber()) +
                " | type:" + safe(room.getType()) +
                " | cap:" + room.getCapacity() +
                " | status:" + safe(room.getStatus()) +
                " | price:" + room.getPricePerNight();
    }

    private String formatNotification(Notification n) {
        String time = n.getCreatedAt() == null ? "-" : LocalDateTime.ofInstant(n.getCreatedAt(), ZoneId.systemDefault()).format(notificationTimeFormatter);
        if (isAdmin()) {
            String target = n.getUserType() == null ? "-" : n.getUserType() + "#" + n.getUserId();
            return n.getId() + " | " + time + " | " + target + " | " + safe(n.getMessage()) + " | read:" + (n.isRead() ? "Y" : "N");
        }
        return n.getId() + " | " + time + " | " + safe(n.getMessage()) + " | read:" + (n.isRead() ? "Y" : "N");
    }

    private void renderAdminNotifications(List<Notification> notifications) {
        adminGroupedNotifications = new ArrayList<>();
        notificationListModel.clear();
        if (notifications == null || notifications.isEmpty()) {
            return;
        }
        java.util.Map<String, List<Notification>> groups = new java.util.LinkedHashMap<>();
        for (Notification n : notifications) {
            String timeKey = n.getCreatedAt() == null ? "-" : String.valueOf(n.getCreatedAt().getEpochSecond());
            String key = n.getMessage() + "|" + timeKey;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(n);
        }
        int i = 1;
        for (List<Notification> group : groups.values()) {
            adminGroupedNotifications.add(group);
            Notification first = group.get(0);
            boolean anyUnread = group.stream().anyMatch(n -> !n.isRead());
            String time = first.getCreatedAt() == null ? "-" : LocalDateTime.ofInstant(first.getCreatedAt(), ZoneId.systemDefault()).format(notificationTimeFormatter);
            String targets = group.stream()
                    .map(n -> (n.getUserType() == null ? "-" : n.getUserType()) + "#" + n.getUserId())
                    .distinct()
                    .sorted()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("-");
            String formatted = "G" + i + " | " + time + " | targets: " + targets + " | " + safe(first.getMessage()) + " | read:" + (anyUnread ? "N" : "Y");
            notificationListModel.addElement(formatted);
            i++;
        }
    }

    private String formatStaff(Staff s) {
        return s.getId() + " | " + safe(s.getDisplayName()) +
                " | user:" + safe(s.getUsername()) +
                " | role:" + safe(s.getRole()) +
                " | email:" + safe(s.getEmail()) +
                " | active:" + (s.isActive() ? "Y" : "N");
    }

    private ListCellRenderer<String> createCustomerRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String name = part(parts, 1);
            String username = part(parts, 2).replace("user:", "").trim();
            String email = part(parts, 3).replace("email:", "").trim();
            String phone = part(parts, 4).replace("phone:", "").trim();
            String nationalId = part(parts, 5).replace("tc:", "").trim();
            String active = part(parts, 6).replace("active:", "").replace("active=", "").trim();
            boolean isActive = active.equalsIgnoreCase("y") || active.equalsIgnoreCase("yes") || active.equalsIgnoreCase("true") || active.equalsIgnoreCase("active");

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel(name + (username.isBlank() ? "" : " (" + username + ")"));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel emailLabel = new JLabel("Email: " + (email.isBlank() ? "-" : email));
            emailLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel phoneLabel = new JLabel("Phone: " + (phone.isBlank() ? "-" : phone));
            phoneLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel idLabel = new JLabel("TC: " + (nationalId.isBlank() ? "-" : nationalId));
            idLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(110, 110, 110));

            JLabel activeLabel = new JLabel(isActive ? "Active" : "Inactive");
            Color statusColor = isActive ? new Color(0, 115, 86) : new Color(150, 33, 33);
            activeLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(activeLabel, BorderLayout.EAST);

            JPanel middleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            middleLine.setOpaque(false);
            middleLine.add(emailLabel);
            middleLine.add(new JLabel("\u2022"));
            middleLine.add(phoneLabel);

            JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bottomLine.setOpaque(false);
            bottomLine.add(idLabel);

            row.add(topLine, BorderLayout.NORTH);
            row.add(middleLine, BorderLayout.CENTER);
            row.add(bottomLine, BorderLayout.SOUTH);
            return row;
        };
    }

    private ListCellRenderer<String> createRoomRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String roomNumber = part(parts, 1).replace("room", "").trim();
            String type = part(parts, 2).replace("type:", "").trim();
            String capacity = part(parts, 3).replace("cap:", "").trim();
            String status = part(parts, 4).replace("status:", "").trim();
            String price = part(parts, 5).replace("price:", "").trim();

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel("Room " + roomNumber + (type.isBlank() ? "" : " \u2022 " + type));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel capacityLabel = new JLabel("Capacity: " + (capacity.isBlank() ? "-" : capacity));
            capacityLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel priceLabel = new JLabel("Price/Night: " + price);
            priceLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel statusLabel = new JLabel(status.isBlank() ? "status unknown" : status);
            String statusLower = status.toLowerCase();
            Color statusColor = statusLower.contains("available") ? new Color(0, 115, 86)
                    : statusLower.contains("reserve") ? new Color(181, 128, 30)
                    : statusLower.contains("maintenance") ? new Color(31, 90, 150)
                    : new Color(110, 110, 110);
            statusLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(statusLabel, BorderLayout.EAST);

            JPanel middleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            middleLine.setOpaque(false);
            middleLine.add(capacityLabel);
            middleLine.add(new JLabel("\u2022"));
            middleLine.add(priceLabel);

            row.add(topLine, BorderLayout.NORTH);
            row.add(middleLine, BorderLayout.CENTER);
            return row;
        };
    }

    private ListCellRenderer<String> createStaffRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String name = part(parts, 1);
            String username = part(parts, 2).replace("user:", "").trim();
            String role = part(parts, 3).replace("role:", "").trim();
            String email = part(parts, 4).replace("email:", "").trim();
            String active = part(parts, 5).replace("active:", "").replace("active=", "").trim();
            boolean isActive = active.equalsIgnoreCase("y") || active.equalsIgnoreCase("yes") || active.equalsIgnoreCase("true") || active.equalsIgnoreCase("active");

            JPanel row = new JPanel(new BorderLayout(6, 4));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 235)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            row.setOpaque(true);
            row.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

            JLabel title = new JLabel(name + (username.isBlank() ? "" : " (" + username + ")"));
            title.setFont(list.getFont().deriveFont(Font.BOLD));
            title.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

            JLabel roleLabel = new JLabel("Role: " + (role.isBlank() ? "-" : role));
            roleLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel emailLabel = new JLabel("Email: " + (email.isBlank() ? "-" : email));
            emailLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel activeLabel = new JLabel(isActive ? "Active" : "Inactive");
            Color statusColor = isActive ? new Color(0, 115, 86) : new Color(150, 33, 33);
            activeLabel.setForeground(isSelected ? list.getSelectionForeground() : statusColor);

            JPanel topLine = new JPanel(new BorderLayout());
            topLine.setOpaque(false);
            topLine.add(title, BorderLayout.WEST);
            topLine.add(activeLabel, BorderLayout.EAST);

            JPanel bottomLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            bottomLine.setOpaque(false);
            bottomLine.add(roleLabel);
            bottomLine.add(new JLabel("\u2022"));
            bottomLine.add(emailLabel);

            row.add(topLine, BorderLayout.NORTH);
            row.add(bottomLine, BorderLayout.CENTER);
            return row;
        };
    }

    private ListCellRenderer<String> createNotificationRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String time = parts.length > 1 ? part(parts, 1) : "";
            String target = parts.length > 4 ? part(parts, 2) : "";
            String message = parts.length > 3 ? part(parts, parts.length > 4 ? 3 : 2) : "";
            String read = part(parts, parts.length - 1).replace("read:", "").trim();
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

            JLabel targetLabel = new JLabel(target);
            targetLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(110, 110, 110));

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
            if (isAdmin() && !target.isBlank()) {
                bottom.add(new JLabel("\u2022"));
                bottom.add(targetLabel);
            }

            row.add(top, BorderLayout.NORTH);
            row.add(bottom, BorderLayout.CENTER);
            return row;
        };
    }

    private ListCellRenderer<String> createStaffReservationRenderer() {
        return (list, value, index, isSelected, cellHasFocus) -> {
            String[] parts = value == null ? new String[0] : value.split("\\|");
            String id = part(parts, 0);
            String customer = part(parts, 1);
            String room = part(parts, 2);
            String dates = part(parts, 3);
            String status = part(parts, 4).replace("status:", "").trim();
            String payment = part(parts, 5).replace("payment:", "").trim();

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

            JLabel customerLabel = new JLabel(customer);
            customerLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel datesLabel = new JLabel(dates);
            datesLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(70, 70, 70));

            JLabel statusLabel = new JLabel("Status: " + status);
            String statusLower = status.toLowerCase();
            Color statusColor = statusLower.contains("cancel") ? new Color(150, 33, 33)
                    : statusLower.contains("pending") ? new Color(181, 128, 30)
                    : statusLower.contains("check") ? new Color(0, 115, 86)
                    : new Color(0, 115, 86);
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
            middleLine.add(customerLabel);
            middleLine.add(new JLabel("\u2022"));
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

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String part(String[] parts, int index) {
        return index < parts.length ? parts[index].trim() : "";
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
