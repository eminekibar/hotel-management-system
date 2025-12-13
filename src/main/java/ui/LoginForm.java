package ui;

import model.user.Customer;
import model.user.Staff;
import service.AuthService;
import service.CustomerService;

import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {
    private final JTextField identifierField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<String> userTypeBox = new JComboBox<>(new String[]{"Customer", "Staff"});
    private final AuthService authService = new AuthService();
    private final CustomerService customerService = new CustomerService();

    public LoginForm() {
        setTitle("Hotel Management - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 220);
        setLocationRelativeTo(null);
        buildUi();
        setVisible(true);
    }

    private void buildUi() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("User Type"), gbc);
        gbc.gridx = 1;
        panel.add(userTypeBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Username / Email / National ID"), gbc);
        gbc.gridx = 1;
        panel.add(identifierField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> doLogin());
        JButton registerButton = new JButton("Register Customer");
        registerButton.addActionListener(e -> openRegistrationDialog());

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(loginButton, gbc);
        gbc.gridx = 1;
        panel.add(registerButton, gbc);

        add(panel);
    }

    private void doLogin() {
        String identifier = identifierField.getText().trim();
        String password = new String(passwordField.getPassword());
        String userType = (String) userTypeBox.getSelectedItem();

        if ("Customer".equals(userType)) {
            Customer customer = authService.loginCustomer(identifier, password);
            if (customer != null) {
                new CustomerPanel(customer);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Customer not found or wrong credentials");
            }
        } else {
            Staff staff = authService.loginStaff(identifier, password);
            if (staff != null) {
                new StaffPanel(staff);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Staff not found or wrong credentials");
            }
        }
    }

    private void openRegistrationDialog() {
        CustomerRegistrationDialog dialog = new CustomerRegistrationDialog(this, customerService);
        dialog.setVisible(true);
    }
}
