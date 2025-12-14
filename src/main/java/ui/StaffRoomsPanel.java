package ui;

import model.room.Room;
import service.RoomService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StaffRoomsPanel extends JPanel {

    private final JFrame owner;
    private final RoomService roomService;

    private final DefaultListModel<String> roomListModel = new DefaultListModel<>();
    private List<Room> cachedRooms;
    private List<Room> filteredRooms;
    private final JTextField roomSearchField = new JTextField();
    private final JComboBox<String> roomTypeFilter = new JComboBox<>(new String[]{"All", "standard", "suite", "family"});
    private final JComboBox<String> roomStatusFilter = new JComboBox<>(new String[]{"All", "available", "reserved", "maintenance", "inactive"});
    private final JSpinner roomCapacityFilter = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
    private final JTextField roomNumberField = new JTextField();
    private final JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"standard", "suite", "family"});
    private final JSpinner roomCapacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
    private final JSpinner roomPriceSpinner = new JSpinner(new SpinnerNumberModel(100.0, 10.0, 5000.0, 10.0));
    private final JTabbedPane tabs = new JTabbedPane();

    public StaffRoomsPanel(JFrame owner, RoomService roomService) {
        this.owner = owner;
        this.roomService = roomService;
        buildUi();
    }

    private void buildUi() {
        tabs.addTab("List & Manage", manageRoomsPanel());
        tabs.addTab("Create New", createRoomPanel());
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel manageRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JList<String> list = new JList<>(roomListModel);
        list.setCellRenderer(StaffListRenderers.createRoomRenderer());
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
        toCreate.addActionListener(e -> tabs.setSelectedIndex(1));
        actions.add(setAvailable);
        actions.add(setMaintenance);
        actions.add(setInactive);
        actions.add(refresh);
        actions.add(toCreate);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRoomPanel() {
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
            tabs.setSelectedIndex(0);
        });
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> clearRoomCreateForm());
        form.add(addRoomButton);
        form.add(clear);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    public void refreshRooms() {
        cachedRooms = roomService.listRooms();
        applyRoomFilters();
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

    private void addRoom() {
        Room room = roomService.addRoom(
                roomNumberField.getText().trim(),
                (String) roomTypeBox.getSelectedItem(),
                (Integer) roomCapacitySpinner.getValue(),
                ((Double) roomPriceSpinner.getValue())
        );
        JOptionPane.showMessageDialog(owner, "Room added: " + room.getRoomNumber());
        clearRoomCreateForm();
        refreshRooms();
    }

    private void updateRoomStatus(int selectedIndex, String status) {
        if (selectedIndex < 0 || filteredRooms == null || selectedIndex >= filteredRooms.size()) {
            JOptionPane.showMessageDialog(owner, "Select a room first");
            return;
        }
        Room room = filteredRooms.get(selectedIndex);
        try {
            roomService.updateStatus(room.getId(), status);
            JOptionPane.showMessageDialog(owner, "Room " + room.getRoomNumber() + " set to " + status);
            refreshRooms();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to update room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearRoomCreateForm() {
        roomNumberField.setText("");
        roomTypeBox.setSelectedIndex(0);
        roomCapacitySpinner.setValue(2);
        roomPriceSpinner.setValue(100.0);
    }

    private String formatRoom(Room room) {
        return room.getId() + " | room " + safe(room.getRoomNumber()) +
                " | type:" + safe(room.getType()) +
                " | cap:" + room.getCapacity() +
                " | status:" + safe(room.getStatus()) +
                " | price:" + room.getPricePerNight();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
