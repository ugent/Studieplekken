package blok2.model.reservables;

import java.util.Objects;

public class Location implements Cloneable {
    private String name;
    private String address;
    private int numberOfSeats;
    private int numberOfLockers;
    private String imageUrl;
    private int authorityId;

    public Location(String name, String address, int numberOfSeats, int numberOfLockers, String imageUrl, int authorityId) {
        this.name = name;
        this.address = address;
        this.numberOfSeats = numberOfSeats;
        this.numberOfLockers = numberOfLockers;
        this.imageUrl = imageUrl;
        this.authorityId = authorityId;
    }

    // default constructor necessary for testing purposes
    public Location() {
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
                Objects.equals(imageUrl, location.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public Location clone() {
        try {
            Location l = (Location) super.clone();
            return l;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public int getNumberOfLockers() {
        return numberOfLockers;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setImageUrl(String url) {
        this.imageUrl = url;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public void setNumberOfLockers(int numberOfLockers) {
        this.numberOfLockers = numberOfLockers;
    }

    public int getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(int authorityId) {
        this.authorityId = authorityId;
    }

    //</editor-fold>
}
