package blok2.model.calendar;

import blok2.helpers.YearWeekDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.threeten.extra.YearWeek;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "timeslots")
public class Timeslot implements Cloneable {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(
                    name = "timeslotSequenceNumber",
                    column = @Column(name = "sequence_number")
            ),
    })
    private final TimeslotId timeslotId;

    @Column(name= "opening_hour")
    private LocalTime openingHour;

    @Column(name= "closing_hour")
    private LocalTime closingHour;


    @Column(name = "seat_count")
    @NotNull
    @Min(0)
    private Integer seatCount;

    @Column(name= "isoday_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name="iso_week")
    private int week;

    @Column(name="iso_year")
    private int year;

    @Column(name = "reservable")
    private boolean reservable;

    @Column(name="reservable_from")
    private LocalDateTime reservableFrom;

    @Column(name="location_id")
    private Integer locationId;

    @Column(name = "reservation_count")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int amountOfReservations;

    // Artefact for framework
    public Timeslot() {
        this(0, null, null, null, false, null, 0, 0);
    }

    public Timeslot(int timeslotSequenceNumber,
                    LocalDate timeslotDate,
                    LocalTime openingHour,
                    LocalTime closingHour,
                    boolean reservable,
                    LocalDateTime reservableFrom,
                    int seatCount,
                    int locationId) {
        this.timeslotId = new TimeslotId(timeslotSequenceNumber);
        this.setTimeslotDate(timeslotDate);
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.reservable = reservable;
        this.reservableFrom = reservableFrom;
        this.seatCount = seatCount;
        this.locationId = locationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return timeslotId.equals(timeslot.timeslotId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
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

    public int getTimeslotSeqnr() {
        return timeslotId.timeslotSequenceNumber;
    }

    public void setTimeslotSeqnr(int timeslotSeqnr) {
        timeslotId.timeslotSequenceNumber = timeslotSeqnr;
    }

    public void setOpeningHour(LocalTime openingHour) {
        this.openingHour = openingHour;
    }

    public LocalTime getClosingHour() {
            return closingHour;
        }

    public void decrementAmountOfReservations() {
        amountOfReservations--;
    }

    public void incrementAmountOfReservations() {
        amountOfReservations++;
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

    @JsonProperty
    @JsonDeserialize(using = YearWeekDeserializer.class)
    public YearWeek getWeek() {
        return YearWeek.of(this.year, this.week);
    }

    public void setWeek(YearWeek week) {
        this.year = week.getYear();
        this.week = week.getWeek();
    }

    @JsonProperty
    public LocalDate timeslotDate() {
        return getWeek().atDay(dayOfWeek);
    }

    public void setTimeslotDate(LocalDate date) {
        this.setWeek(YearWeek.from(date));
        this.dayOfWeek = DayOfWeek.from(date);
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

    @Embeddable
    public static class TimeslotId implements Serializable {
        Integer timeslotSequenceNumber;

        public TimeslotId() {}

        public TimeslotId(int timeslotSequenceNumber) {
            this.timeslotSequenceNumber = timeslotSequenceNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeslotId that = (TimeslotId) o;
                   return Objects.equals(timeslotSequenceNumber, that.timeslotSequenceNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeslotSequenceNumber);
        }
    }

}
