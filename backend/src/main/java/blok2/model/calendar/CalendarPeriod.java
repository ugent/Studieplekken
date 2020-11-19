package blok2.model.calendar;

import blok2.helpers.Resources;
import blok2.model.reservables.Location;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.SECONDS;

public class CalendarPeriod extends Period implements Cloneable {
    private Integer id;
    private Location location;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private LocalDateTime reservableFrom = LocalDateTime.now();
    private LocalDateTime lockedFrom;
    private boolean reservable;
    private int reservableTimeslotSize;

    private List<Timeslot> timeslots = Collections.emptyList();

    public CalendarPeriod() { }

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
                Duration.between(this.reservableFrom, that.reservableFrom).toMillis() <= 1000;
                // One second precision is enough.
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

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public LocalDateTime getReservableFrom() {
        return reservableFrom;
    }

    public void setReservableFrom(LocalDateTime reservableFrom) {
        if(reservableFrom != null)
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getLockedFrom() {
        return lockedFrom;
    }

    public void setLockedFrom(LocalDateTime lockedFrom) {
        this.lockedFrom = lockedFrom;
    }

    public void initializeLockedFrom() {
        lockedFrom = this.getStartsAt().minusWeeks(3)
                    .with(DayOfWeek.of(Integer.parseInt(Resources.blokatugentConf.getString("lockedFromDayOfWeek"))))
                .atTime(LocalTime.now());
    }

    public boolean isLocked() {
        return getLockedFrom().isBefore(LocalDateTime.now());
    }
    /**
     * The length of time the location is open (in seconds)
     */
    public int getOpenHoursDuration() {
        return Math.toIntExact(SECONDS.between(getOpeningTime(), getClosingTime()));
    }
}
