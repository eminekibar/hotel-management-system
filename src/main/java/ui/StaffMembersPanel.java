package ui;

import model.user.Staff;
import service.StaffService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StaffMembersPanel extends JPanel {

    private final JFrame owner;
    private final StaffService staffService;
    private final boolean isAdmin;

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
    private final JTabbedPane tabs = new JTabbedPane();

    public StaffMembersPanel(JFrame owner, StaffService staffService, boolean isAdmin) {
        this.owner = owner;
        this.staffService = staffService;
        this.isAdmin = isAdmin;
        buildUi();
    }

    private void buildUi() {
        tabs.addTab("List & Manage", manageStaffPanel());
        tabs.addTab("Create New", createStaffPanel());
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel manageStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JList<String> list = new JList<>(staffListModel);
        list.setCellRenderer(StaffListRenderers.createStaffRenderer());
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
        toCreate.addActionListener(e -> tabs.setSelectedIndex(1));
        actions.add(deactivate);
        actions.add(refresh);
        actions.add(toCreate);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStaffPanel() {
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
        addBtn.addActionListener(e -> registerStaff());
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> clearStaffCreateForm());
        form.add(addBtn);
        form.add(clear);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    public void refreshStaff() {
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

    private void registerStaff() {
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
            JOptionPane.showMessageDialog(owner, "Staff created: " + created.getUsername());
            clearStaffCreateForm();
            refreshStaff();
            tabs.setSelectedIndex(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to create staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        if (!isAdmin) {
            JOptionPane.showMessageDialog(owner, "Only admin can deactivate staff.");
            return;
        }
        if (index < 0 || visibleStaff == null || index >= visibleStaff.size()) {
            JOptionPane.showMessageDialog(owner, "Select a staff first");
            return;
        }
        Staff target = visibleStaff.get(index);
        if (target.getUsername() != null && target.getUsername().equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(owner, "Admin account cannot be deactivated.");
            return;
        }
        if (!target.isActive()) {
            JOptionPane.showMessageDialog(owner, "Staff is already inactive.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(owner,
                "Deactivate staff " + target.getDisplayName() + "?",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            staffService.deactivate(target.getId());
            JOptionPane.showMessageDialog(owner, "Staff deactivated.");
            refreshStaff();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to deactivate staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatStaff(Staff s) {
        return s.getId() + " | " + safe(s.getDisplayName()) +
                " | user:" + safe(s.getUsername()) +
                " | role:" + safe(s.getRole()) +
                " | email:" + safe(s.getEmail()) +
                " | active:" + (s.isActive() ? "Y" : "N");
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
