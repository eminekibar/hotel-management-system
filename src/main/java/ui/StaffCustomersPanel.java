package ui;

import model.user.Customer;
import service.CustomerService;
import service.ReservationService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StaffCustomersPanel extends JPanel {

    private final JFrame owner;
    private final CustomerService customerService;
    private final ReservationService reservationService;

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
    private final JTabbedPane tabs = new JTabbedPane();

    public StaffCustomersPanel(JFrame owner, CustomerService customerService, ReservationService reservationService) {
        this.owner = owner;
        this.customerService = customerService;
        this.reservationService = reservationService;
        buildUi();
    }

    private void buildUi() {
        tabs.addTab("List & Manage", manageCustomersPanel());
        tabs.addTab("Create New", createCustomerPanel());
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel manageCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JList<String> list = new JList<>(customerListModel);
        list.setCellRenderer(StaffListRenderers.createCustomerRenderer());
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
        actions.add(details);
        actions.add(deactivate);
        actions.add(refresh);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCustomerPanel() {
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
        create.addActionListener(e -> registerCustomer());
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> clearCustomerCreateForm());
        form.add(create);
        form.add(clear);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    public void refreshCustomers() {
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

    private void registerCustomer() {
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
            JOptionPane.showMessageDialog(owner, "Customer created: " + created.getUsername());
            clearCustomerCreateForm();
            refreshCustomers();
            tabs.setSelectedIndex(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to create customer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(owner, "Select a customer first");
            return;
        }
        Customer customer = visibleCustomers.get(index);
        if (!customer.isActive()) {
            JOptionPane.showMessageDialog(owner, "Customer is already inactive.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(owner,
                "Deactivate customer " + customer.getDisplayName() + "? Existing reservations stay active.",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            customerService.deactivate(customer.getId());
            JOptionPane.showMessageDialog(owner, "Customer deactivated.");
            refreshCustomers();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to deactivate customer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCustomerDetails(int index) {
        if (index < 0 || visibleCustomers == null || index >= visibleCustomers.size()) {
            JOptionPane.showMessageDialog(owner, "Select a customer first");
            return;
        }
        Customer customer = visibleCustomers.get(index);
        CustomerDetailDialog dialog = new CustomerDetailDialog(owner, customer, reservationService);
        dialog.setVisible(true);
    }

    private String formatCustomer(Customer c) {
        return c.getId() + " | " + c.getDisplayName() +
                " | user:" + safe(c.getUsername()) +
                " | email:" + safe(c.getEmail()) +
                " | phone:" + safe(c.getPhone()) +
                " | tc:" + safe(c.getNationalId()) +
                " | active:" + (c.isActive() ? "Y" : "N");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
