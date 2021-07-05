package blok2.model.calendar;

import blok2.helpers.Resources;
import blok2.model.reservables.Location;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.time.*;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;

@Entity
@Table(name = "calendar_periods")
public class CalendarPeriod extends Period implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private Location location;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "reservable_from")
    private LocalDateTime reservableFrom = LocalDateTime.now();

    @Column(name = "locked_from")
    private LocalDateTime lockedFrom;

    @Column(name = "reservable")
    private boolean reservable;

    @Column(name = "timeslot_length")
    private int timeslotLength;

    @Column(name = "seat_count")
    private int seatCount;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", referencedColumnName = "calendar_id")
    private List<Timeslot> timeslots = Collections.emptyList();

    public CalendarPeriod() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarPeriod that = (CalendarPeriod) o;
        return reservable == that.reservable &&
                timeslotLength == that.timeslotLength &&
                seatCount == that.seatCount &&
                Objects.equals(id, that.id) &&
                Objects.equals(location, that.location) &&
                Objects.equals(getStartsAt(), that.getStartsAt()) &&
                Objects.equals(getEndsAt(), that.getEndsAt()) &&
                Objects.equals(openingTime, that.openingTime) &&
                Objects.equals(closingTime, that.closingTime) &&
                Duration.between(this.reservableFrom, that.reservableFrom).toMillis() <= 1000 && // One second precision is enough.
                Objects.equals(lockedFrom, that.lockedFrom) &&
                Objects.equals(timeslots, that.timeslots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, getStartsAt(), getEndsAt(), openingTime, closingTime,
                reservableFrom, lockedFrom, reservable, timeslotLength, seatCount, timeslots);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
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
        if (reservableFrom != null)
            this.reservableFrom = reservableFrom;
    }

    public boolean isReservable() {
        return reservable;
    }

    public void setReservable(boolean reservable) {
        this.reservable = reservable;
    }

    public int getTimeslotLength() {
        return timeslotLength;
    }

    public void setTimeslotLength(int timeslotLength) {
        this.timeslotLength = timeslotLength;
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

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

}
