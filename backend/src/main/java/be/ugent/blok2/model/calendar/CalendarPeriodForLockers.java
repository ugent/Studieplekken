package be.ugent.blok2.model.calendar;

import be.ugent.blok2.model.reservables.Location;

import java.util.Objects;

public class CalendarPeriodForLockers extends Period implements Cloneable {
    private Location location;
    private String reservableFrom; // datetime: YYYY-MM-DDThh:mm

    public CalendarPeriodForLockers() {

    }

    @Override
    public String toString() {
        return "CalendarPeriodForLockers{" +
                "location=" + location +
                ", startsAt='" + getStartsAt() + '\'' +
                ", endsAt='" + getEndsAt() + '\'' +
                ", reservableFrom='" + reservableFrom + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarPeriodForLockers that = (CalendarPeriodForLockers) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(getStartsAt(), that.getStartsAt()) &&
                Objects.equals(getEndsAt(), that.getEndsAt()) &&
                Objects.equals(reservableFrom, that.reservableFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, getStartsAt(), getEndsAt(), reservableFrom);
    }

    @Override
    public CalendarPeriodForLockers clone() {
        try {
            CalendarPeriodForLockers clone = (CalendarPeriodForLockers) super.clone();
            clone.location = location.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getReservableFrom() {
        return reservableFrom;
    }

    public void setReservableFrom(String reservableFrom) {
        this.reservableFrom = reservableFrom;
    }
}
