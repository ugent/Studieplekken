package blok2.model.calendar;

import org.springframework.beans.factory.annotation.Required;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

public class Timeslot implements Cloneable {
    @Min(0)
    @NotNull
    private Integer calendarId;
    @Min(0)
    @NotNull
    private Integer timeslotSeqnr;
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}")
    @NotBlank
    private String timeslotDate;


    public Timeslot() {

    }

    public Timeslot(int calendarId, int timeslotSeqnr, String timeslotDate) {
        this.calendarId = calendarId;
        this.timeslotSeqnr = timeslotSeqnr;
        this.timeslotDate = timeslotDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return calendarId == timeslot.calendarId;
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

    public String getTimeslotDate() {
        return timeslotDate;
    }

    public void setTimeslotDate(String timeslotDate) {
        this.timeslotDate = timeslotDate;
    }
}
