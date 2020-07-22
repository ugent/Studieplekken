package be.ugent.blok2.model.reservables;

import java.util.Objects;

public class Locker implements Cloneable {
    private int id;
    private int number; // identification number of locker
    private Location location;

    public Locker() {

    }

    @Override
    public String toString() {
        return "Locker{" +
                ", number=" + number +
                ", location=" + location +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locker locker = (Locker) o;
        return number == locker.number &&
                Objects.equals(location, locker.getLocation()) &&
                id == locker.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, id);
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
