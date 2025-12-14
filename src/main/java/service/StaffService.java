package service;

import dao.StaffDAO;
import model.user.Staff;
import util.HashUtil;

import java.util.List;
import java.util.regex.Pattern;

public class StaffService {

    private final StaffDAO staffDAO;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.-]{3,20}$");

    public StaffService() {
        this.staffDAO = new StaffDAO();
    }

    public Staff register(String username, String firstName, String lastName, String email, String nationalId, String role, String rawPassword) {
        validateRegistration(username, firstName, lastName, email, nationalId, role, rawPassword);
        Staff staff = new Staff(username, firstName, lastName, email, nationalId, HashUtil.hashPassword(rawPassword), role);
        staffDAO.create(staff);
        return staff;
    }

    public List<Staff> listStaff() {
        return staffDAO.findAll();
    }

    public List<Staff> searchStaff(String term) {
        if (term == null || term.isBlank()) {
            return staffDAO.findAll();
        }
        return staffDAO.search(term);
    }

    public void deactivate(int id) {
        staffDAO.deactivate(id);
    }

    public Staff findByIdentifier(String identifier) {
        return staffDAO.findByIdentifier(identifier);
    }

    private void validateRegistration(String username, String firstName, String lastName, String email, String nationalId, String role, String rawPassword) {
        if (isBlank(username) || isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(nationalId) || isBlank(role) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("All fields are required.");
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
        if (staffDAO.existsByUsernameOrEmailOrNationalId(username, email, nationalId)) {
            throw new IllegalArgumentException("Username, email or national ID already exists.");
        }
        validatePassword(rawPassword);
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
