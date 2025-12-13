package service;

import dao.CustomerDAO;
import dao.StaffDAO;
import model.user.Customer;
import model.user.Staff;

public class AuthService {

    private final CustomerDAO customerDAO;
    private final StaffDAO staffDAO;

    public AuthService() {
        this.customerDAO = new CustomerDAO();
        this.staffDAO = new StaffDAO();
    }

    public Customer loginCustomer(String identifier, String passwordHash) {
        Customer customer = customerDAO.findByIdentifier(identifier);
        if (customer != null && passwordHash.equals(customer.getPasswordHash())) {
            return customer;
        }
        return null;
    }

    public Staff loginStaff(String identifier, String passwordHash) {
        Staff staff = staffDAO.findByIdentifier(identifier);
        if (staff != null && passwordHash.equals(staff.getPasswordHash())) {
            return staff;
        }
        return null;
    }
}
