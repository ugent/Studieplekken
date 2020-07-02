package be.ugent.blok2.reservables;


import java.util.Objects;

public class Locker {
    private int id;
    private int number; // identification number of locker
    private String location;
    private int studentLimit;

    public Locker() {

    }

    public Locker(int number, String location, int studentLimit) {
        this.number = number;
        this.location = location;
        this.studentLimit = studentLimit;
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
                location.equalsIgnoreCase(locker.location) &&
                id == locker.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, id);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getStudentLimit() {
        return studentLimit;
    }

    public void setStudentLimit(int studentLimit) {
        this.studentLimit = studentLimit;
    }

    //</editor-fold>
}
