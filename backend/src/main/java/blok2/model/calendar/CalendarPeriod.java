package blok2.model.calendar;

import blok2.model.reservables.Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CalendarPeriod extends Period implements Cloneable {
    private int id;
    private Location location;
    private String openingTime; // time: hh:mm
    private String closingTime; // time: hh:mm
    private String reservableFrom; // datetime: YYYY-MM-DDThh:mm
    private boolean reservable;
    private int reservableTimeslotSize;

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

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(List<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getStartdateAsDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(getStartsAt()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch(ParseException e) {
            // This really shouldn't happen: it was checked in 'analyze'
            throw new RuntimeException("The date was unparseable in a place where it should have been checked!");
        }
    }

    public LocalDate getEndDateAsDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(getEndsAt()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch(ParseException e) {
            // This really shouldn't happen: it was checked in 'analyze'
            throw new RuntimeException("The date was unparseable in a place where it should have been checked!");
        }
    }

    /**
     * The length of time the location is open (in seconds)
     * @return
     */
    public int getOpenHoursDuration() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date startDate = null;
        try {
            startDate = format.parse(getStartsAt() + " " + getOpeningTime());
            Date endDate = format.parse(getStartsAt() + " " + getClosingTime());
            return Math.toIntExact(endDate.getTime() - startDate.getTime()) / 1000;
        } catch (ParseException e) {
            // This really shouldn't happen: it was checked in 'analyze'
            throw new RuntimeException("The date was unparseable in a place where it should have been checked!");
        }

    }
}
