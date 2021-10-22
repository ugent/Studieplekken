package blok2.model.calendar;

import blok2.helpers.CustomLocalDateTimeDeserializer;
import blok2.helpers.YearWeekDeserializer;
import blok2.helpers.orm.CustomLocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
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
import java.util.UUID;

@Entity
@Table(name = "timeslots")
public class Timeslot implements Cloneable {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "sequence_number")
    @NotNull
    private Integer timeslotSequenceNumber;

    @Column(name= "opening_hour")
    private LocalTime openingHour;

    @Column(name= "closing_hour")
    private LocalTime closingHour;


    @Column(name = "seat_count")
    @NotNull
    @Min(0)
    private Integer seatCount;

    @Column(name="timeslot_date")
    private LocalDate timeslotDate;

    @Column(name = "reservable")
    private boolean reservable;

    @Column(name="reservable_from")
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reservableFrom;

    @Column(name="location_id")
    @NotNull
    private Integer locationId;

    @Column(name = "reservation_count")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int amountOfReservations;

    @Column(name="timeslot_group_id")
    private UUID timeslotGroup;

    @Column(name="repeatable")
    private boolean repeatable;

    // Artefact for framework
    public Timeslot() {
        this(null, null, null, null, false, null, 0, 0);
    }

    public Timeslot(Integer timeslotSequenceNumber,
                    LocalDate timeslotDate,
                    LocalTime openingHour,
                    LocalTime closingHour,
                    boolean reservable,
                    LocalDateTime reservableFrom,
                    int seatCount,
                    int locationId) {
        this.timeslotSequenceNumber = timeslotSequenceNumber;
        if(timeslotDate != null)
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
        return Objects.equals(timeslotSequenceNumber, timeslot.timeslotSequenceNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeslotSequenceNumber);
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

    public Integer getTimeslotSeqnr() {
        return timeslotSequenceNumber;
    }

    public void setTimeslotSeqnr(Integer i) {
        timeslotSequenceNumber = i;
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

    public boolean isReservable() {
        return reservable;
    }

    public void setReservable(boolean reservable) {
        this.reservable = reservable;
    }


    @JsonProperty
    @Transient
    public LocalDate timeslotDate() {
        return this.timeslotDate;
    }

    @Transient
    public void setTimeslotDate(LocalDate date) {
        this.timeslotDate = date;
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

    public UUID getTimeslotGroup() {
        return timeslotGroup;
    }

    public void setTimeslotGroup(UUID timeslotGroup) {
        this.timeslotGroup = timeslotGroup;
    }

    public boolean isRepeatable() {
        return this.repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
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
