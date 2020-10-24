package blok2.model.calendar;

import blok2.model.reservables.Location;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CalendarPeriod extends Period implements Cloneable {
    private Location location;
    private String openingTime; // time: hh:mm
    private String closingTime; // time: hh:mm
    private String reservableFrom; // datetime: YYYY-MM-DDThh:mm
    private boolean reservable;

    // Helper variables
    private int reservableTimeslotSize;
    private int length;

    private List<Timeslot> timeslots = Collections.emptyList();

    public CalendarPeriod() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CalendarPeriod that = (CalendarPeriod) o;
        return reservable == that.reservable &&
                reservableTimeslotSize == that.reservableTimeslotSize &&
                location.equals(that.location) &&
                openingTime.equals(that.openingTime) &&
                closingTime.equals(that.closingTime) &&
                reservableFrom.equals(that.reservableFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), location, openingTime, closingTime, reservableFrom, reservable, reservableTimeslotSize);
    }


    @Override
    public String toString() {
        return "CalendarPeriod{" +
                "location=" + location +
                ", startsAt='" + getStartsAt() + '\'' +
                ", endsAt='" + getEndsAt() + '\'' +
                ", openingTime='" + openingTime + '\'' +
                ", closingTime='" + closingTime + '\'' +
                ", reservableFrom='" + reservableFrom + '\'' +
                ", reservable='" + reservable + '\'' +
                ", reservableTimeslotSize='" + reservableTimeslotSize + '\'' +
                '}';
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


    public boolean isReservable() {
        return reservable;
    }

    public void setReservable(boolean reservable) {
        this.reservable = reservable;
    }

    public int getReservableTimeslotSize() {
        return reservableTimeslotSize;
    }

    public void setReservableTimeslotSize(int reservableTimeslotSize) {
        this.reservableTimeslotSize = reservableTimeslotSize;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(List<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }
}
