package be.ugent.blok2.model.reservations;

import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.users.User;

import java.util.Objects;


public class LocationReservation {
    private User user;
    private Location location;
    private CustomDate date;
    private Boolean attended;

    public LocationReservation() {
    }

    public LocationReservation(Location location, User user, CustomDate date) {
        this.location = location;
        this.user = user;
        this.date = date;
        this.attended = null;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public User getUser() {
        return user;
    }

    public Location getLocation() {
        return location;
    }

    public CustomDate getDate() {
        return date;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setDate(CustomDate date) {
        this.date = date;
    }

    public Boolean getAttended() {
        return attended;
    }

    public void setAttended(Boolean attended) {
        this.attended = attended;
    }

    //</editor-fold>

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationReservation that = (LocationReservation) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(location, that.location) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, location, date);
    }
}
