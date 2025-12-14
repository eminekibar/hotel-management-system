package ui;

import model.user.Customer;
import service.CustomerService;
import service.NotificationQueryService;
import service.ReservationService;
import service.RoomService;

import javax.swing.*;
import java.awt.*;

public class CustomerPanel extends JFrame {

    private final Customer customer;
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final ReservationService reservationService = new ReservationService();
    private final NotificationQueryService notificationQueryService = new NotificationQueryService();

    private final HistoryPanel historyPanel;
    private final ReservationsPanel reservationsPanel;
    private final BookStayPanel bookStayPanel;
    private final ProfilePanel profilePanel;
    private final NotificationsPanel notificationsPanel;

    public CustomerPanel(Customer customer) {
        this.customer = customer;
        this.historyPanel = new HistoryPanel(customer, reservationService);
        this.reservationsPanel = new ReservationsPanel(this, customer, reservationService, historyPanel::refreshHistory);
        this.bookStayPanel = new BookStayPanel(this, customer, roomService, reservationService,
                reservationsPanel::refreshReservations, historyPanel::refreshHistory);
        this.profilePanel = new ProfilePanel(this, customer, customerService, this::logout);
        this.notificationsPanel = new NotificationsPanel(this, customer, notificationQueryService);

        setTitle("Customer Panel - " + customer.getDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setLocationRelativeTo(null);
        buildUi();
        reservationsPanel.refreshReservations();
        historyPanel.refreshHistory();
        notificationsPanel.refreshNotifications();
        setVisible(true);
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book a Stay", bookStayPanel);
        tabs.addTab("My Reservations", reservationsPanel);
        tabs.addTab("History", historyPanel);
        tabs.addTab("Profile", profilePanel);
        tabs.addTab("Notifications", notificationsPanel);
        tabs.setSelectedIndex(0);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, " + customer.getDisplayName());
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
