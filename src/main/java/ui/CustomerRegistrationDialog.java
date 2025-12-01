package ui;

import model.user.Customer;
import service.CustomerService;

import javax.swing.*;
import java.awt.*;

public class CustomerRegistrationDialog extends JDialog {

    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField nationalIdField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final CustomerService customerService;

    public CustomerRegistrationDialog(Frame owner, CustomerService customerService) {
        super(owner, "Register Customer", true);
        this.customerService = customerService;
        setSize(400, 320);
        setLocationRelativeTo(owner);
        buildUi();
    }

    private void buildUi() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(panel, gbc, row++, "First Name", firstNameField);
        addRow(panel, gbc, row++, "Last Name", lastNameField);
        addRow(panel, gbc, row++, "Email", emailField);
        addRow(panel, gbc, row++, "Phone", phoneField);
        addRow(panel, gbc, row++, "National ID", nationalIdField);
        addRow(panel, gbc, row++, "Password", passwordField);

        JButton submit = new JButton("Create Account");
        submit.addActionListener(e -> submit());
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(submit, gbc);
        add(panel);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void submit() {
        Customer customer = customerService.register(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                nationalIdField.getText().trim(),
                new String(passwordField.getPassword())
        );
        JOptionPane.showMessageDialog(this, "Account created for " + customer.getDisplayName());
        dispose();
    }
}
