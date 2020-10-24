package blok2.model.calendar;

import java.util.Objects;

public class Timeslot implements Cloneable {
    private int calendarId;
    private int timeslotSeqnr;
    private String timeslotDate;

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
