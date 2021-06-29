package blok2.model.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "timeslots")
public class Timeslot implements Cloneable {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(
                    name = "calendarId",
                    column = @Column(name = "calendar_id")
            ),
            @AttributeOverride(
                    name = "timeslotSequenceNumber",
                    column = @Column(name = "timeslot_sequence_number")
            ),
            @AttributeOverride(
                    name = "timeslotDate",
                    column = @Column(name = "timeslot_date")
            ),
    })
    private final TimeslotId timeslotId;

    @Column(name = "seat_count")
    @NotNull
    @Min(0)
    private Integer seatCount;

    @Column(name = "reservation_count")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int amountOfReservations;

    // Artefact for framework
    public Timeslot() {
        this(0, 0, null, 0, 0);
    }

    public Timeslot(int calendarId,
                    int timeslotSequenceNumber,
                    LocalDate timeslotDate,
                    int seatCount,
                    int amountOfReservations) {
        this.timeslotId = new TimeslotId(calendarId, timeslotSequenceNumber, timeslotDate);
        this.seatCount = seatCount;
        this.amountOfReservations = amountOfReservations;
    }

    public Timeslot(int calendarId, int timeslotSeqnr, LocalDate timeslotDate, int seatCount) {
        this.timeslotId = new TimeslotId(calendarId, timeslotSeqnr, timeslotDate);
        this.seatCount = seatCount;
    }

    public Timeslot(CalendarPeriod period, int timeslotSeqnr, LocalDate timeslotDate) {
        this(period.getId(), timeslotSeqnr, timeslotDate, period.getSeatCount());
    }

    public static int compareTo(Timeslot var1, Timeslot var2) {
        if (var1.timeslotId.timeslotDate.equals(var2.timeslotId.timeslotDate)) {
            return Integer.compare(var1.timeslotId.timeslotSequenceNumber, var2.timeslotId.timeslotSequenceNumber);
        } else {
            return var1.timeslotId.timeslotDate.compareTo(var2.timeslotId.timeslotDate);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return timeslotId.calendarId.equals(timeslot.timeslotId.calendarId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeslotId.calendarId);
    }

    public int getCalendarId() {
        return timeslotId.calendarId;
    }

    public void setCalendarId(int calendarId) {
        timeslotId.calendarId = calendarId;
    }

    public int getTimeslotSeqnr() {
        return timeslotId.timeslotSequenceNumber;
    }

    public void setTimeslotSeqnr(int timeslotSeqnr) {
        timeslotId.timeslotSequenceNumber = timeslotSeqnr;
    }

    public LocalDate getTimeslotDate() {
        return timeslotId.timeslotDate;
    }

    public void setTimeslotDate(LocalDate timeslotDate) {
        timeslotId.timeslotDate = timeslotDate;
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

    @Embeddable
    public static class TimeslotId implements Serializable {
        Integer calendarId;
        Integer timeslotSequenceNumber;
        LocalDate timeslotDate;

        public TimeslotId() {}

        public TimeslotId(int calendarId, int timeslotSequenceNumber, LocalDate timeslotDate) {
            this.calendarId = calendarId;
            this.timeslotSequenceNumber = timeslotSequenceNumber;
            this.timeslotDate = timeslotDate;
        }
    }

}
