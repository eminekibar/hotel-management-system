package model.user;

public class Staff extends BaseUser {
    private String role;
    private String nationalId;
    private boolean active = true;

    public Staff() {
    }

    public Staff(String username, String firstName, String lastName, String email, String nationalId, String passwordHash, String role) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.nationalId = nationalId;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
        return firstName + " " + lastName + " (" + role + ")";
    }
}
