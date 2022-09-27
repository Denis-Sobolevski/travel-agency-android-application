package com.example.tourexpert;

/**
 * this class is a model representation for the User,
 * email - email of the user
 * firstName - first name of the user
 * lastName - last name of the user
 * phone - phone number of the user
 * type - the type of the account: user / employee / admin
 * status - true: user is active to use the application, false: user is not allowed to login or attempt re-login
 * key - unique identifier, the Node Key(primary key) upon creation
 */
public class User {

    protected String email;
    protected String firstName;
    protected String lastName;
    protected String phone;
    protected String type;
    protected boolean status;
    protected String key;

    public User() {

    }

    public User(String email, String firstName, String lastName, String phone, String type, boolean status, String key) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.type = type;
        this.status = status;
        this.key = key;
    }

    // region getters:
    public String getKey() {
        return this.key;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getType() {
        return this.type;
    }

    public boolean getStatus() {
        return this.status;
    }

    // endregion

    // region setters:
    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    // endregion

    @Override
    public String toString() {
        return "User:\n" +
                "email='" + email + "\n" +
                ", firstName='" + firstName + "\n" +
                ", lastName='" + lastName + "\n" +
                ", phone='" + phone + "\n" +
                ", type='" + type + "\n" +
                ", status=" + status + "\n"+
                ", key='" + key + "\n";
    }
}
