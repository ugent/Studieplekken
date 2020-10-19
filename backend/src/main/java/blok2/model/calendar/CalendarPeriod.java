package blok2.model.calendar;

import blok2.daos.db.DBCalendarPeriodDao;
import blok2.helpers.Pair;
import blok2.model.reservables.Location;
import java.time.LocalDateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

public class CalendarPeriod extends Period implements Cloneable {
    private Location location;
    private String openingTime; // time: hh:mm
    private String closingTime; // time: hh:mm
    private String reservableFrom; // datetime: YYYY-MM-DDThh:mm

    private final Logger logger = Logger.getLogger(CalendarPeriod.class.getSimpleName());


    public CalendarPeriod() {

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
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarPeriod that = (CalendarPeriod) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(getStartsAt(), that.getStartsAt()) &&
                Objects.equals(getEndsAt(), that.getEndsAt()) &&
                Objects.equals(openingTime, that.openingTime) &&
                Objects.equals(closingTime, that.closingTime) &&
                Objects.equals(reservableFrom, that.reservableFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartsAt(), getEndsAt(), openingTime, closingTime, reservableFrom);
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

    public Pair<LocalDateTime, LocalDateTime> getBeginAndEndDate(){
        String beginDT = String.format("%s %s", this.getStartsAt(), this.getOpeningTime());
        String endDT = String.format("%s %s", this.getEndsAt(), this.getClosingTime());

        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime begin = LocalDateTime.parse(beginDT, myFormatObj);
        LocalDateTime end = LocalDateTime.parse(endDT, myFormatObj);
        return new Pair<>(begin, end);
    }

    public boolean isActive() {
        final String FORMAT = "yyyy-MM-dd";
        DateFormat formatter = new SimpleDateFormat(FORMAT);
        try {
            return formatter.parse(this.getStartsAt()).compareTo(new Date()) <= 0 &&
                    formatter.parse(this.getEndsAt()).compareTo(new Date()) >= 0;
        } catch (ParseException e) {
            this.logger.severe("isActive(): Problem while parsing the begin or enddate of calendarperiod");
            return false;
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
}
