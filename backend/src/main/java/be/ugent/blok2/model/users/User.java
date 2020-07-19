package be.ugent.blok2.model.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;


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
    private String barcode;
    private Role[] roles;

    // The locations a scanner is allowed to scan at
    private Collection<String> scannerLocations;

    public User() {
        this.scannerLocations = new ArrayList<>();
    }

    public User(String augentID){

        this.augentID = augentID;
        this.scannerLocations = new ArrayList<>();
    }

    public User(String augentID, String lastName, String firstName
            , String mail, String password, String institution,
                Role[] roles, String barcode) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.mail = mail;
        this.password = password;
        this.institution = institution;
        this.augentID = augentID;
        this.roles = roles;
        this.penaltyPoints = 0;
        this.barcode=barcode;
        scannerLocations= new ArrayList<>();
    }


    public User(String augentID, String lastName, String firstName
            , String mail, String password, String institution,
                Role[] roles, Integer penaltyPoints, String barcode) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.mail = mail;
        this.password = password;
        this.institution = institution;
        this.augentID = augentID;
        this.roles = roles;
        this.penaltyPoints = penaltyPoints;
        this.barcode=barcode;
        scannerLocations = new ArrayList<>();
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
                Arrays.equals(roles, user.roles);
    }

    @Override
    public int hashCode() {
        return (augentID).hashCode();
    }

    public User clone() {
        try {
            User c = (User) super.clone();

            c.roles = new Role[roles.length];
            for (int i = 0; i < roles.length; i++) {
                c.roles[i] = roles[i];
            }

            c.scannerLocations = new ArrayList<>();
            c.scannerLocations.addAll(scannerLocations);

            return c;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public User cloneToSendableUser() {
        try {
            User c = (User) super.clone();
            c.password = "";
            c.roles = new Role[roles.length];
            for (int i = 0; i < roles.length; i++) {
                c.roles[i] = roles[i];
            }
            c.scannerLocations = new ArrayList<>();
            return c;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void addScanLocation(String l){
        this.scannerLocations.add(l);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<Authority> authorities = new ArrayList<>();
        for (int i = 0; i < roles.length; i++) {
            authorities.add(new Authority(roles[i]));
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return mail;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public String getLastName() {
        return lastName;
    }

    public void setScannerLocations(Collection<String> scannerLocations) {
        this.scannerLocations = scannerLocations;
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

    public Role[] getRoles() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getBarcode(){
        return barcode;
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

    public void setRoles(Role[] roles) {
        this.roles = roles;
    }

    public void setBarcode(String barcode){
        this.barcode=barcode;
    }

    public Collection<String> getScannerLocations() {
        return scannerLocations;
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
                ", penaltyPoints=" + penaltyPoints +
                ", barcode='" + barcode + '\'' +
                ", roles=" + Arrays.toString(roles) +
                '}';
    }

    public String shortString() {
        return this.getAugentID()+" " + this.getFirstName()+ " " + this.getLastName();
    }
}
