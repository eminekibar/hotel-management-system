package builder;

import model.user.Customer;

public class CustomerBuilder {
    private final Customer.Builder builder = Customer.builder();

    public CustomerBuilder username(String username) {
        builder.username(username);
        return this;
    }

    public CustomerBuilder firstName(String firstName) {
        builder.firstName(firstName);
        return this;
    }

    public CustomerBuilder lastName(String lastName) {
        builder.lastName(lastName);
        return this;
    }

    public CustomerBuilder email(String email) {
        builder.email(email);
        return this;
    }

    public CustomerBuilder passwordHash(String passwordHash) {
        builder.passwordHash(passwordHash);
        return this;
    }

    public CustomerBuilder phone(String phone) {
        builder.phone(phone);
        return this;
    }

    public CustomerBuilder nationalId(String nationalId) {
        builder.nationalId(nationalId);
        return this;
    }

    public Customer build() {
        return builder.build();
    }
}
