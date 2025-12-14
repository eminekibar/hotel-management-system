package ui;

import model.reservation.Reservation;
import model.room.RoomAvailabilityInfo;
import model.room.Room;
import model.user.Customer;
import service.ReservationService;
import service.RoomService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class BookStayPanel extends JPanel {

    private final JFrame owner;
    private final Customer customer;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final Runnable reservationsRefresher;
    private final Runnable historyRefresher;

    private final JTextField startDateField = new JTextField();
    private final JTextField endDateField = new JTextField();
    private final JComboBox<String> roomTypeBox = new JComboBox<>(new String[]{"standard", "suite", "family"});
    private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
    private final DefaultListModel<String> searchResultsModel = new DefaultListModel<>();
    private List<RoomAvailabilityInfo> lastAvailabilityResults;
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");

    public BookStayPanel(JFrame owner,
                         Customer customer,
                         RoomService roomService,
                         ReservationService reservationService,
                         Runnable reservationsRefresher,
                         Runnable historyRefresher) {
        this.owner = owner;
        this.customer = customer;
        this.roomService = roomService;
        this.reservationService = reservationService;
        this.reservationsRefresher = reservationsRefresher;
        this.historyRefresher = historyRefresher;
        buildUi();
        setDefaultDates();
    }

    private void buildUi() {
        setLayout(new BorderLayout());
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
        add(form, BorderLayout.NORTH);

        JList<String> resultList = new JList<>(searchResultsModel);
        resultList.setCellRenderer(CustomerListRenderers.createRoomResultRenderer());
        add(new JScrollPane(resultList), BorderLayout.CENTER);

        JButton reserveButton = new JButton("Book Selected");
        reserveButton.addActionListener(e -> reserveSelected(resultList.getSelectedIndex()));
        add(reserveButton, BorderLayout.SOUTH);
    }

    public void setDefaultDates() {
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
        JDialog dialog = new JDialog(owner, "Select date", true);
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
        dialog.setLocationRelativeTo(owner);

        final LocalDate[] currentMonth = {baseDate.withDayOfMonth(1)};
        Runnable render = () -> {
            monthLabel.setText(currentMonth[0].format(monthFormatter));
            daysPanel.removeAll();
            int startOffset = (currentMonth[0].getDayOfWeek().getValue() + 6) % 7;
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
            java.time.DayOfWeek dow = java.time.DayOfWeek.of(i == 7 ? 7 : i);
            JLabel lbl = new JLabel(dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()), SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
            header.add(lbl);
        }
        return header;
    }

    private void searchRooms() {
        LocalDate start = parseDateField(startDateField, "Check-in date");
        LocalDate end = parseDateField(endDateField, "Check-out date");
        if (start == null || end == null) {
            return;
        }
        if (!end.isAfter(start)) {
            JOptionPane.showMessageDialog(owner, "Check-out must be after check-in.");
            return;
        }
        try {
            String type = (String) roomTypeBox.getSelectedItem();
            int capacity = (Integer) capacitySpinner.getValue();
            java.util.List<RoomAvailabilityInfo> availabilityList = roomService.searchWithAvailability(type, capacity, start, end);
            java.util.List<RoomAvailabilityInfo> bookable = new java.util.ArrayList<>();
            searchResultsModel.clear();
            for (RoomAvailabilityInfo info : availabilityList) {
                if (!info.isBookable()) {
                    continue;
                }
                bookable.add(info);
                Room room = info.getRoom();
                searchResultsModel.addElement(
                        room.getRoomNumber() + " | " + room.getType() +
                                " | capacity " + room.getCapacity() +
                                " | $" + room.getPricePerNight() +
                                " | " + info.getAvailabilityLabel()
                );
            }
            lastAvailabilityResults = bookable;
            if (bookable.isEmpty()) {
                JOptionPane.showMessageDialog(owner, "No rooms available for the selected dates and filters.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Invalid search input: " + ex.getMessage());
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
                JOptionPane.showMessageDialog(owner, "Check-out must be after check-in.");
                return;
            }
            RoomAvailabilityInfo info = lastAvailabilityResults.get(index);
            if (!info.isBookable()) {
                JOptionPane.showMessageDialog(owner, "Selected room is not available for these dates.");
                return;
            }
            Room room = info.getRoom();
            Reservation reservation = reservationService.createReservation(customer, room, start, end);
            JOptionPane.showMessageDialog(owner, "Reservation created: " + reservation.getReservationId());
            if (reservationsRefresher != null) {
                reservationsRefresher.run();
            }
            if (historyRefresher != null) {
                historyRefresher.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to reserve: " + ex.getMessage());
        }
    }

    private void setDateField(JTextField field, LocalDate date) {
        field.setText(date.format(displayFormatter));
    }

    private LocalDate parseDateField(JTextField field, String label) {
        try {
            return LocalDate.parse(field.getText().trim(), displayFormatter);
        } catch (Exception e) {
            if (label != null) {
                JOptionPane.showMessageDialog(owner, label + " is not valid. Please pick a date.");
            }
            return null;
        }
    }
}
