package blok2.model.reservables;

import blok2.model.Authority;
import blok2.model.LocationTag;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

public class Location implements Cloneable {
    private String name;
    private String address;
    private int numberOfSeats;
    private int numberOfLockers;
    private String imageUrl;
    private String descriptionDutch = "";
    private String descriptionEnglish= "";

    private Authority authority;
    private String status;

    private List<LocationTag> assignedTags;

    public Location(String name, String address, int numberOfSeats, int numberOfLockers, String imageUrl,
                    Authority authority, String descriptionDutch, String descriptionEnglish,
                    List<LocationTag> assignedTags, String status) {
        this.name = name;
        this.address = address;
        this.numberOfSeats = numberOfSeats;
        this.numberOfLockers = numberOfLockers;
        this.imageUrl = imageUrl;
        this.descriptionDutch = descriptionDutch;
        this.descriptionEnglish = descriptionEnglish;
        this.authority = authority;
        this.assignedTags = assignedTags;
        this.status = status;
    }

    // default constructor necessary for testing purposes
    public Location() {
        assignedTags = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return numberOfSeats == location.numberOfSeats &&
                numberOfLockers == location.numberOfLockers &&
                Objects.equals(name, location.name) &&
                Objects.equals(address, location.address) &&
                Objects.equals(imageUrl, location.imageUrl) &&
                Objects.equals(descriptionDutch, location.descriptionDutch) &&
                Objects.equals(descriptionEnglish, location.descriptionEnglish) &&
                Objects.equals(authority, location.authority) &&
                Objects.equals(status, location.status) &&
                (assignedTags == null || assignedTags.equals(location.assignedTags));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public Location clone() {
        try {
            Location l = (Location) super.clone();

            // deep copy the mutable attributes
            l.authority = authority.clone();
            l.assignedTags = new ArrayList<>(assignedTags.size());

            for (int i = 0; i < assignedTags.size(); i++) {
                l.assignedTags.set(i, assignedTags.get(i).clone());
            }

            return l;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public int getNumberOfLockers() {
        return numberOfLockers;
    }

    public void setNumberOfLockers(int numberOfLockers) {
        this.numberOfLockers = numberOfLockers;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescriptionDutch() {
        return descriptionDutch;
    }

    public void setDescriptionDutch(String descriptionDutch) {
        this.descriptionDutch = descriptionDutch;
    }

    public String getDescriptionEnglish() {
        return descriptionEnglish;
    }

    public void setDescriptionEnglish(String descriptionEnglish) {
        this.descriptionEnglish = descriptionEnglish;
    }

    public Authority getAuthority() {
        return authority;
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    public List<LocationTag> getAssignedTags() {
        return assignedTags;
    }

    public void setAssignedTags(List<LocationTag> assignedTags) {
        this.assignedTags = assignedTags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    //</editor-fold>

}
