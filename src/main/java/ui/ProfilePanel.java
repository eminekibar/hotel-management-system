package ui;

import model.user.Customer;
import service.CustomerService;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {

    private final JFrame owner;
    private final Customer customer;
    private final CustomerService customerService;
    private final Runnable logoutAction;

    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JPasswordField newPasswordField = new JPasswordField();

    public ProfilePanel(JFrame owner,
                        Customer customer,
                        CustomerService customerService,
                        Runnable logoutAction) {
        this.owner = owner;
        this.customer = customer;
        this.customerService = customerService;
        this.logoutAction = logoutAction;
        buildUi();
    }

    private void buildUi() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addInfoRow(infoPanel, gbc, row++, "Username:", new JLabel(customer.getUsername()));
        addInfoRow(infoPanel, gbc, row++, "First Name:", new JLabel(customer.getFirstName()));
        addInfoRow(infoPanel, gbc, row++, "Last Name:", new JLabel(customer.getLastName()));
        addInfoRow(infoPanel, gbc, row++, "National ID:", new JLabel(customer.getNationalId()));

        emailField.setText(customer.getEmail());
        addInfoRow(infoPanel, gbc, row++, "Email:", emailField);
        phoneField.setText(customer.getPhone());
        addInfoRow(infoPanel, gbc, row++, "Phone:", phoneField);

        add(infoPanel);

        JButton save = new JButton("Update Contact");
        save.addActionListener(e -> {
            customer.setEmail(emailField.getText().trim());
            customer.setPhone(phoneField.getText().trim());
            customerService.updateProfile(customer);
            JOptionPane.showMessageDialog(owner, "Profile updated");
        });
        JPanel contactButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        contactButtons.add(save);
        add(contactButtons);

        JPanel passwordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(4, 6, 4, 6);
        pgbc.anchor = GridBagConstraints.LINE_START;
        pgbc.fill = GridBagConstraints.HORIZONTAL;
        pgbc.weightx = 0;
        pgbc.gridx = 0; pgbc.gridy = 0;
        passwordPanel.add(new JLabel("New Password:"), pgbc);
        pgbc.weightx = 1;
        pgbc.gridx = 1;
        passwordPanel.add(newPasswordField, pgbc);
        JButton changePassword = new JButton("Change Password");
        changePassword.addActionListener(e -> updatePassword());
        pgbc.weightx = 0;
        pgbc.gridx = 2;
        passwordPanel.add(changePassword, pgbc);
        add(passwordPanel);

        JButton deleteAccount = new JButton("Delete Account");
        deleteAccount.addActionListener(e -> deleteAccount());
        JPanel deleteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        deleteRow.add(deleteAccount);
        add(deleteRow);
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.weightx = 1;
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private void updatePassword() {
        String newPassword = new String(newPasswordField.getPassword()).trim();
        try {
            customerService.changePassword(customer.getId(), newPassword);
            newPasswordField.setText("");
            JOptionPane.showMessageDialog(owner, "Password updated");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(owner, ex.getMessage(), "Validation error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to update password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(owner,
                "Are you sure you want to deactivate your account?\nExisting reservations will stay active.",
                "Confirm Deactivation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            customerService.deleteAccount(customer.getId());
            JOptionPane.showMessageDialog(owner, "Account deactivated. Existing reservations remain active.");
            if (logoutAction != null) {
                logoutAction.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Failed to deactivate account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
