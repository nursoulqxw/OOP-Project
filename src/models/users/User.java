package models.users;

import core.observer.Observer;
import core.observer.NewsEvent;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


public abstract class User implements Observer, Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private LocalDateTime createdAt;

    protected User(String id, String firstName, String lastName,
                   String email, String password, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }

    public boolean authenticate(String pwd) {
        return this.password != null && this.password.equals(pwd);
    }


    public void sendMessage(User to, String msg) {
        if (to == null || msg == null) return;
        System.out.printf("[message] %s -> %s: %s%n",
                this.getFullName(), to.getFullName(), msg);
    }


    public abstract String getRole();

    @Override
    public void update(NewsEvent event) {
        if (event == null || event.getNews() == null) return;
        System.out.printf("[news -> %s] %s%n",
                this.getFullName(), event.getNews());
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    //----

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', email='%s'}",
                getRole(), id, getFullName(), email);
    }
}