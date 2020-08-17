package be.ugent.blok2.model.calendar;

import be.ugent.blok2.model.reservables.Location;

import java.util.Objects;

public class CalendarPeriodForLockers implements Cloneable {
    private Location location;
    private String startsAt; // date: YYYY-MM-DD
    private String endsAt; // date: YYYY-MM-DD
    private String reservableFrom; // datetime: YYYY-MM-DDThh:mm

    public CalendarPeriodForLockers() {

    }

    @Override
    public String toString() {
        return "CalendarPeriodForLockers{" +
                "location=" + location +
                ", startsAt='" + startsAt + '\'' +
                ", endsAt='" + endsAt + '\'' +
                ", reservableFrom='" + reservableFrom + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarPeriodForLockers that = (CalendarPeriodForLockers) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(startsAt, that.startsAt) &&
                Objects.equals(endsAt, that.endsAt) &&
                Objects.equals(reservableFrom, that.reservableFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, startsAt, endsAt, reservableFrom);
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

    public String getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(String startsAt) {
        this.startsAt = startsAt;
    }

    public String getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(String endsAt) {
        this.endsAt = endsAt;
    }

    public String getReservableFrom() {
        return reservableFrom;
    }

    public void setReservableFrom(String reservableFrom) {
        this.reservableFrom = reservableFrom;
    }
}
