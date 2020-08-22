package be.ugent.blok2.model.calendar;

import be.ugent.blok2.model.reservables.Location;

import java.util.Objects;

public class CalendarPeriod implements Cloneable {
    private Location location;
    private String startsAt; // date: YYYY-MM-DD
    private String endsAt; // date: YYYY-MM-DD
    private String openingTime; // time: hh:mm
    private String closingTime; // time: hh:mm
    private String reservableFrom; // datetime: YYYY-MM-DDThh:mm

    public CalendarPeriod() {

    }

    @Override
    public String toString() {
        return "CalendarPeriod{" +
                "location=" + location +
                ", startsAt='" + startsAt + '\'' +
                ", endsAt='" + endsAt + '\'' +
                ", openingTime='" + openingTime + '\'' +
                ", closingTime='" + closingTime + '\'' +
                ", reservableFrom='" + reservableFrom + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarPeriod that = (CalendarPeriod) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(startsAt, that.startsAt) &&
                Objects.equals(endsAt, that.endsAt) &&
                Objects.equals(openingTime, that.openingTime) &&
                Objects.equals(closingTime, that.closingTime) &&
                Objects.equals(reservableFrom, that.reservableFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startsAt, endsAt, openingTime, closingTime, reservableFrom);
    }

    @Override
    public CalendarPeriod clone() {
        try {
            CalendarPeriod clone = (CalendarPeriod) super.clone();
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

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getReservableFrom() {
        return reservableFrom;
    }

    public void setReservableFrom(String reservableFrom) {
        this.reservableFrom = reservableFrom;
    }
}
