package be.ugent.blok2.model.reservables;

import java.util.Objects;

public class Locker implements Cloneable {
    //private int id;
    private int number; // identification number of locker
    private Location location;

    public Locker() {

    }

    @Override
    public String toString() {
        return "Locker{" +
                ", number=" + number +
                ", location=" + location +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locker locker = (Locker) o;
        return number == locker.number &&
                Objects.equals(location, locker.getLocation());
        // Note: do not include the id, this is just a number that the
        // database uses for PK. This has no further meaning to the application
    }

    @Override
    public int hashCode() {
        return Objects.hash(number/*, id*/);
    }

    @Override
    public Locker clone() {
        try {
            return (Locker) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() { return number; }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    //</editor-fold>
}
