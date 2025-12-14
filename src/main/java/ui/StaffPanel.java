package ui;

import model.user.Staff;
import service.CustomerService;
import service.NotificationQueryService;
import service.ReservationService;
import service.RoomService;
import service.StaffService;

import javax.swing.*;
import java.awt.*;

public class StaffPanel extends JFrame {

    private final Staff staff;
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final ReservationService reservationService = new ReservationService();
    private final StaffService staffService = new StaffService();
    private final NotificationQueryService notificationQueryService = new NotificationQueryService();

    private final StaffCustomersPanel customersPanel;
    private final StaffMembersPanel staffMembersPanel;
    private final StaffRoomsPanel roomsPanel;
    private final StaffReservationsPanel reservationsPanel;
    private final StaffNotificationsPanel notificationsPanel;

    public StaffPanel(Staff staff) {
        this.staff = staff;
        this.customersPanel = new StaffCustomersPanel(this, customerService, reservationService);
        this.staffMembersPanel = isAdmin() ? new StaffMembersPanel(this, staffService, true) : null;
        this.roomsPanel = new StaffRoomsPanel(this, roomService);
        this.reservationsPanel = new StaffReservationsPanel(this, staff, reservationService, roomService, customerService, roomsPanel::refreshRooms);
        this.notificationsPanel = new StaffNotificationsPanel(this, staff, notificationQueryService, isAdmin());

        setTitle("Staff Panel - " + staff.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        buildUi();
        customersPanel.refreshCustomers();
        if (isAdmin() && staffMembersPanel != null) {
            staffMembersPanel.refreshStaff();
        }
        roomsPanel.refreshRooms();
        reservationsPanel.refreshReservations();
        notificationsPanel.refreshNotifications();
        setVisible(true);
    }

    private boolean isAdmin() {
        return staff.getUsername() != null && staff.getUsername().equalsIgnoreCase("admin");
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Customers", customersPanel);
        if (isAdmin() && staffMembersPanel != null) {
            tabs.addTab("Staff", staffMembersPanel);
        }
        tabs.addTab("Rooms", roomsPanel);
        tabs.addTab("Reservations", reservationsPanel);
        tabs.addTab("Notifications", notificationsPanel);
        int reservationsTabIndex = tabs.indexOfTab("Reservations");
        if (reservationsTabIndex >= 0) {
            tabs.setSelectedIndex(reservationsTabIndex);
        }
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
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
        return header;
    }

    private void logout() {
        dispose();
        new LoginForm();
    }
}
