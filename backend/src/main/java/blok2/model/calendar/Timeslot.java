package blok2.model.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Timeslot implements Cloneable {
    @NotNull
    private CalendarPeriod period;

    private LocalTime startTime;

    private LocalTime endTime;

    @Min(0)
    @NotNull
    private Integer timeslotSeqnr;

    @NotNull
    @Min(0)
    private Integer seatCount;

    private Integer dayOfWeek;
    private boolean reservable;


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int amountOfReservations;

    // Artefact for framework
    public Timeslot() {}

    public Timeslot(CalendarPeriod period, int timeslotSeqnr) {
        setPeriod(period);
        setTimeslotSeqnr(timeslotSeqnr);
    }

    public Timeslot(CalendarPeriod period, @Min(0) @NotNull Integer timeslotSeqnr, Integer dayOfWeek, LocalTime startTime,  LocalTime endTime, boolean reservable, @NotNull @Min(0) Integer seatCount, int amountOfReservations) {
        this.period = period;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeslotSeqnr = timeslotSeqnr;
        this.seatCount = seatCount;
        this.dayOfWeek = dayOfWeek;
        this.reservable = reservable;
        this.amountOfReservations = amountOfReservations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return getPeriod().equals(timeslot.getPeriod()) && getTimeslotSeqnr() == timeslot.getTimeslotSeqnr();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int getTimeslotSeqnr() {
        return timeslotSeqnr;
    }

    public void setTimeslotSeqnr(int timeslotSeqnr) {
        this.timeslotSeqnr = timeslotSeqnr;
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public CalendarPeriod getPeriod() {
        return period;
    }

    public void setPeriod(CalendarPeriod period) {
        this.period = period;
    }

    public boolean isReservable() {
        return reservable;
    }

    public void setReservable(boolean reservable) {
        this.reservable = reservable;
    }

    public LocalDateTime getStartDate() {
        return period.getWeek().atDay(DayOfWeek.of(dayOfWeek)).atTime(startTime);
    }

    public LocalDateTime getEndDate() {
        return period.getWeek().atDay(DayOfWeek.of(dayOfWeek)).atTime(endTime);
    }

}
