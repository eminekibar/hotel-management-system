package service;

import builder.CustomerBuilder;
import dao.CustomerDAO;
import model.user.Customer;

public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public Customer register(String username, String firstName, String lastName, String email, String phone, String nationalId, String passwordHash) {
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

    public void changePassword(int id, String newPasswordHash) {
        Customer customer = customerDAO.findById(id);
        if (customer != null) {
            customer.setPasswordHash(newPasswordHash);
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
}
