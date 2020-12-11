package blok2.model.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

public class Timeslot implements Cloneable {
    @Min(0)
    @NotNull
    private Integer calendarId;
    @Min(0)
    @NotNull
    private Integer timeslotSeqnr;
    @NotNull
    private LocalDate timeslotDate;

    @NotNull
    @Min(0)
    private Integer seatCount;


    @JsonProperty(access= JsonProperty.Access.READ_ONLY)
    private int amountOfReservations;

    public Timeslot(int calendarId, int timeslotSeqnr, LocalDate timeslotDate) {
        this.calendarId = calendarId;
        this.timeslotSeqnr = timeslotSeqnr;
        this.timeslotDate = timeslotDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return calendarId.equals(timeslot.calendarId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calendarId);
    }

    public int getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(int calendarId) {
        this.calendarId = calendarId;
    }

    public int getTimeslotSeqnr() {
        return timeslotSeqnr;
    }

    public void setTimeslotSeqnr(int timeslotSeqnr) {
        this.timeslotSeqnr = timeslotSeqnr;
    }

    public LocalDate getTimeslotDate() {
        return timeslotDate;
    }

    public void setTimeslotDate(LocalDate timeslotDate) {
        this.timeslotDate = timeslotDate;
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
}
