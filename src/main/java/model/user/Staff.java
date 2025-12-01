package model.user;

public class Staff extends BaseUser {
    private String role;

    public Staff() {
    }

    public Staff(String firstName, String lastName, String email, String passwordHash, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName + " (" + role + ")";
    }
}
