package blok2.model.users;

import java.util.Objects;
import java.util.*;

import blok2.model.Authority;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;

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
    @NotNull
    private String augentID;
    private int penaltyPoints;
    private boolean admin;

    // Named 'userAuthorities' instead of 'authorities' because there would be a
    // conflict with the getter UserDetails#getAuthorities() that is used to return
    // the GrantedAuthority objects of the user. These GrantedAuthority objects are used
    // in the controller methods to authorize the user accessing the methods.
    private List<Authority> userAuthorities;

    // List of location IDs the user is volunteer for
    private List<Integer> userVolunteer;

    public User() {
        lastName = "";
        firstName = "";
        mail = "";
        password = "";
        institution = "";
        augentID = "";
        userAuthorities = new ArrayList<>();
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


    public List<Authority> getUserAuthorities() {
        return userAuthorities;
    }
    public List<Integer> getUserVolunteer() {
        return userVolunteer;
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

    public void setUserAuthorities(List<Authority> userAuthorities) {
        this.userAuthorities = userAuthorities;
    }

    public void setUserVolunteer(List<Integer> userVolunteer) {
        this.userVolunteer = userVolunteer;
    }

//</editor-fold>

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    // *********************************************
    // *   Implementation of UserDetails methods   *
    // *********************************************/

    /**
     * A logged in user always has the GrantedAuthority "USER". Depending on the attribute User#admin and
     * whether or not the user is linked to one or more authorities (User#userAuthorities.size() > 0),
     * respectively the GrantedAuthorities "ADMIN" and "HAS_AUTHORITIES" are added.
     *
     * @return list of GrantedAuthority objects
     */
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // user always gets the GrantedAuthority "USER" if the user is logged in
        if (!augentID.isEmpty())
            grantedAuthorities.add(new SimpleGrantedAuthority("USER"));

        // if the user is admin, the GrantedAuthority "ADMIN" is added too
        if (admin)
            grantedAuthorities.add(new SimpleGrantedAuthority("ADMIN"));

        // if the user is linked to one or more authorities, the
        // GrantedAuthority "HAS_AUTHORITIES" is added too
        if (userAuthorities.size() > 0)
            grantedAuthorities.add(new SimpleGrantedAuthority("HAS_AUTHORITIES"));

        if(userVolunteer.size() > 0)
            grantedAuthorities.add(new SimpleGrantedAuthority("HAS_VOLUNTEERS"));

        return grantedAuthorities;
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
