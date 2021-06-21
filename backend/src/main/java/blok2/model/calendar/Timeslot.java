package blok2.model.calendar;

import blok2.helpers.YearWeekDeserializer;
import blok2.model.reservables.Location;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.threeten.extra.YearWeek;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.Calendar;

public class Timeslot implements Cloneable {
    private LocalTime openingHour;

    private LocalTime closingHour;

    @Min(0)
    @NotNull
    private Integer timeslotSequenceNumber;

    @NotNull
    @Min(0)
    private Integer seatCount;

    private DayOfWeek dayOfWeek;
    @JsonDeserialize(using = YearWeekDeserializer.class)
    private YearWeek week;
    private boolean reservable;

    private LocalDateTime reservableFrom;

    private Integer locationId;



    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int amountOfReservations;

    // Artefact for framework
    public Timeslot() {
    }

    public Timeslot(@Min(0) @NotNull Integer timeslotSeqnr, DayOfWeek dayOfWeek, YearWeek week,  LocalTime startTime, LocalTime endTime, boolean reservable, LocalDateTime reservableFrom, @NotNull @Min(0) Integer seatCount, int amountOfReservations, int locationId) {
        this.openingHour = startTime;
        this.closingHour = endTime;
        this.timeslotSequenceNumber = timeslotSeqnr;
        this.seatCount = seatCount;
        this.dayOfWeek = dayOfWeek;
        this.reservable = reservable;
        this.amountOfReservations = amountOfReservations;
        this.week = week;
        this.locationId = locationId;
        this.reservableFrom = reservableFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return getTimeslotSequenceNumber() == timeslot.getTimeslotSequenceNumber();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int getTimeslotSequenceNumber() {
        return timeslotSequenceNumber;
    }

    public void setTimeslotSequenceNumber(int timeslotSequenceNumber) {
        this.timeslotSequenceNumber = timeslotSequenceNumber;
    }

    public int getAmountOfReservations() {
        return amountOfReservations;
    }

    public void setAmountOfReservations(int amountOfReservations) {
        this.amountOfReservations = amountOfReservations;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Integer seatCount) {
        this.seatCount = seatCount;
    }

    public LocalTime getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(LocalTime openingHour) {
        this.openingHour = openingHour;
    }

    public LocalTime getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(LocalTime closingHour) {
        this.closingHour = closingHour;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean isReservable() {
        return reservable;
    }

    public void setReservable(boolean reservable) {
        this.reservable = reservable;
    }

    public void setTimeslotSequenceNumber(Integer timeslotSequenceNumber) {
        this.timeslotSequenceNumber = timeslotSequenceNumber;
    }

    public YearWeek getWeek() {
        return week;
    }

    public void setWeek(YearWeek week) {
        this.week = week;
    }


    public LocalDate timeslotDate() {
        return week.atDay(dayOfWeek);
    }

    public LocalDateTime getReservableFrom() {
        return reservableFrom;
    }

    public void setReservableFrom(LocalDateTime reservableFrom) {
        this.reservableFrom = reservableFrom;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocation(int locationId) {
        this.locationId = locationId;
    }
}
