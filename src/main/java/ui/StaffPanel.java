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
import java.util.List;

public class StaffPanel extends JFrame {

    private final Staff staff;
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final ReservationService reservationService = new ReservationService();

    private final DefaultListModel<String> customerListModel = new DefaultListModel<>();
    private List<Customer> cachedCustomers;
    private final JTextField customerSearchField = new JTextField();

    private final DefaultListModel<String> roomListModel = new DefaultListModel<>();
    private List<Room> cachedRooms;

    private final DefaultListModel<String> reservationListModel = new DefaultListModel<>();
    private List<Reservation> cachedReservations;
    private final JTextField reservationCustomerFilter = new JTextField();
    private final JTextField reservationRoomFilter = new JTextField();

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
    private List<Room> resSearchRooms;

    public StaffPanel(Staff staff) {
        this.staff = staff;
        setTitle("Staff Panel - " + staff.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        buildUi();
        refreshCustomers();
        refreshRooms();
        refreshReservations();
        setVisible(true);
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
        JPanel buttons = new JPanel();
        buttons.add(search);
        buttons.add(addCustomer);
        top.add(buttons, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshCustomers());
        panel.add(refresh, BorderLayout.SOUTH);
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
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> list = new JList<>(reservationListModel);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton checkIn = new JButton("Check-in");
        checkIn.addActionListener(e -> performOnSelected(list.getSelectedIndex(), "checkin"));
        JButton checkOut = new JButton("Check-out");
        checkOut.addActionListener(e -> performOnSelected(list.getSelectedIndex(), "checkout"));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> performOnSelected(list.getSelectedIndex(), "cancel"));
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshReservations());
        buttons.add(checkIn);
        buttons.add(checkOut);
        buttons.add(cancel);
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);

        JPanel filterPanel = new JPanel(new GridLayout(0, 5, 4, 4));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Reservations"));
        filterPanel.add(new JLabel("Customer"));
        filterPanel.add(reservationCustomerFilter);
        filterPanel.add(new JLabel("Room"));
        filterPanel.add(reservationRoomFilter);
        JButton applyFilter = new JButton("Apply");
        applyFilter.addActionListener(e -> refreshReservations());
        filterPanel.add(applyFilter);

        JPanel createPanel = new JPanel(new GridLayout(0, 2, 4, 4));
        createPanel.setBorder(BorderFactory.createTitledBorder("Create Reservation"));
        createPanel.add(new JLabel("Customer ID"));
        createPanel.add(resCustomerIdField);
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

        JPanel header = new JPanel(new BorderLayout());
        header.add(filterPanel, BorderLayout.NORTH);
        header.add(createPanel, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private void refreshCustomers() {
        cachedCustomers = customerService.searchCustomers(customerSearchField.getText().trim());
        customerListModel.clear();
        for (Customer c : cachedCustomers) {
            customerListModel.addElement(c.getId() + " | " + c.getDisplayName() + " | " + c.getUsername() + " | " + c.getEmail());
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
        cachedReservations = reservationService.listReservationsByFilter(
                reservationCustomerFilter.getText().trim(),
                reservationRoomFilter.getText().trim()
        );
        reservationListModel.clear();
        for (Reservation res : cachedReservations) {
            reservationListModel.addElement(
                    res.getReservationId() + " | cust#" + res.getCustomer().getId() + " " + res.getCustomer().getDisplayName() +
                            " | room " + res.getRoom().getRoomNumber() + " (" + res.getRoom().getType() + ")" +
                            " | " + res.getCurrentState().getName()
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
        switch (action) {
            case "checkin" -> reservationService.checkIn(reservation.getReservationId(), staff.getId());
            case "checkout" -> reservationService.checkOut(reservation.getReservationId(), staff.getId());
            case "cancel" -> reservationService.cancelReservation(reservation.getReservationId(), staff.getId());
            default -> {
            }
        }
        refreshReservations();
        refreshRooms();
    }

    private void searchRoomsForReservation() {
        try {
            String type = (String) resRoomTypeBox.getSelectedItem();
            int capacity = (Integer) resCapacitySpinner.getValue();
            LocalDate start = LocalDate.parse(resStartDateField.getText().trim());
            LocalDate end = LocalDate.parse(resEndDateField.getText().trim());
            resSearchRooms = roomService.search(type, capacity, start, end);
            resSearchRoomsModel.clear();
            for (Room room : resSearchRooms) {
                resSearchRoomsModel.addElement(room.getRoomNumber() + " | " + room.getType() + " | " + room.getCapacity());
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
        try {
            int customerId = Integer.parseInt(resCustomerIdField.getText().trim());
            Customer customer = customerService.getProfile(customerId);
            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Customer not found");
                return;
            }
            Room room = resSearchRooms.get(selectedRoomIndex);
            LocalDate start = LocalDate.parse(resStartDateField.getText().trim());
            LocalDate end = LocalDate.parse(resEndDateField.getText().trim());
            Reservation reservation = reservationService.createReservation(customer, room, start, end);
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

    private void logout() {
        dispose();
        new LoginForm();
    }
}
