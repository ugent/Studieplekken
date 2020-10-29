package blok2.model.users;

import java.util.Objects;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * This class is used to represent a registered user or UGhent student.
 * This class implements the interface UserDetails so spring can use this
 * class to verify login credentials.
 */
public class User implements Cloneable, UserDetails {
    private String lastName;
    private String firstName;
    private String mail;
    private String password;
    private String institution;
    private String augentID;
    private int penaltyPoints;
    private boolean admin;

    public User() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(lastName, user.lastName) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(mail.toLowerCase(), user.mail.toLowerCase()) &&
                Objects.equals(institution, user.institution) &&
                Objects.equals(augentID, user.augentID) &&
                admin == user.admin;
    }

    @Override
    public int hashCode() {
        return (augentID).hashCode();
    }

    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMail() {
        return mail;
    }

    public String getInstitution() {
        return institution;
    }

    public String getAugentID() {
        return augentID;
    }

    public int getPenaltyPoints() {
        return penaltyPoints;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAugentID(String augentID) {
        this.augentID = augentID;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setPenaltyPoints(int penaltyPoints) {
        this.penaltyPoints = penaltyPoints;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

//</editor-fold>

    @Override
    public String toString() {
        return "User{" +
                "lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", mail='" + mail + '\'' +
                ", password='" + password + '\'' +
                ", institution='" + institution + '\'' +
                ", augentID='" + augentID + '\'' +
                ", penaltyPoints='" + penaltyPoints + '\'' +
                ", admin='" + admin + '\'' +
                '}';
    }

    /*********************************************
     *   Implementation of UserDetails methods   *
     *********************************************/

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(admin ? "admin" : "user"));
    }

    @Override
    public String getUsername() {
        return mail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
