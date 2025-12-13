package service;

import builder.CustomerBuilder;
import dao.CustomerDAO;
import model.user.Customer;
import util.HashUtil;

import java.util.regex.Pattern;

public class CustomerService {

    private final CustomerDAO customerDAO;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.-]{3,20}$");

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public Customer register(String username, String firstName, String lastName, String email, String phone, String nationalId, String rawPassword) {
        validateRegistration(username, firstName, lastName, email, phone, nationalId, rawPassword);
        String passwordHash = HashUtil.hashPassword(rawPassword);
        Customer customer = new CustomerBuilder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .nationalId(nationalId)
                .passwordHash(passwordHash)
                .build();
        customerDAO.create(customer);
        return customer;
    }

    public Customer getProfile(int id) {
        return customerDAO.findById(id);
    }

    public void updateProfile(Customer customer) {
        customerDAO.update(customer);
    }

    public void changePassword(int id, String newRawPassword) {
        validatePassword(newRawPassword);
        Customer customer = customerDAO.findById(id);
        if (customer != null) {
            customer.setPasswordHash(HashUtil.hashPassword(newRawPassword));
            customerDAO.update(customer);
        }
    }

    public void deactivate(int id) {
        customerDAO.deactivate(id);
    }

    public java.util.List<Customer> listCustomers() {
        return customerDAO.findAll();
    }

    public java.util.List<Customer> searchCustomers(String query) {
        if (query == null || query.isBlank()) {
            return customerDAO.findAll();
        }
        return customerDAO.search(query);
    }

    private void validateRegistration(String username, String firstName, String lastName, String email, String phone, String nationalId, String rawPassword) {
        if (isBlank(username) || isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(nationalId) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("All fields except phone are required.");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username must be 3-20 chars and only letters, numbers, ., _ or -.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!nationalId.matches("\\d{11}")) {
            throw new IllegalArgumentException("National ID must be 11 digits.");
        }
        if (phone != null && !phone.isBlank() && !phone.matches("[+\\d][\\d\\-\\s]{6,}")) {
            throw new IllegalArgumentException("Phone number format is not valid.");
        }
        validatePassword(rawPassword);
        if (customerDAO.existsByUsernameOrEmailOrNationalId(username, email, nationalId)) {
            throw new IllegalArgumentException("Username, email or national ID already exists.");
        }
    }

    private void validatePassword(String rawPassword) {
        if (isBlank(rawPassword)) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (!rawPassword.matches(".*[A-Za-z].*") || !rawPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must include letters and numbers.");
        }
        if (rawPassword.contains(" ")) {
            throw new IllegalArgumentException("Password cannot contain spaces.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
