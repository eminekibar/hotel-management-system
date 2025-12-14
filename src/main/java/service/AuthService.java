package service;

import dao.CustomerDAO;
import dao.StaffDAO;
import model.user.Customer;
import model.user.Staff;
import util.HashUtil;

public class AuthService {

    private final CustomerDAO customerDAO;
    private final StaffDAO staffDAO;

    public AuthService() {
        this.customerDAO = new CustomerDAO();
        this.staffDAO = new StaffDAO();
    }

    public Customer loginCustomer(String identifier, String rawPassword) {
        Customer customer = customerDAO.findByIdentifier(identifier);
        if (customer != null && customer.isActive() && HashUtil.isPasswordMatch(rawPassword, customer.getPasswordHash())) {
            return customer;
        }
        return null;
    }

    public Staff loginStaff(String identifier, String rawPassword) {
        Staff staff = staffDAO.findByIdentifier(identifier);
        if (staff != null && staff.isActive() && HashUtil.isPasswordMatch(rawPassword, staff.getPasswordHash())) {
            return staff;
        }
        return null;
    }
}
