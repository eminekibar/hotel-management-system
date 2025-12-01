package model.user;

public class Customer extends BaseUser {
    private String phone;
    private String nationalId;
    private boolean active;

    public Customer() {
        this.active = true;
    }

    private Customer(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.phone = builder.phone;
        this.nationalId = builder.nationalId;
        this.active = true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    public static class Builder {
        private String firstName;
        private String lastName;
        private String email;
        private String passwordHash;
        private String phone;
        private String nationalId;

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder nationalId(String nationalId) {
            this.nationalId = nationalId;
            return this;
        }

        public Customer build() {
            return new Customer(this);
        }
    }
}
