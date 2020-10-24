package blok2.model.calendar;

import java.util.Objects;

public class Timeslot implements Cloneable {
    private int timeslotId;
    private int timeslotSeqnr;

    private int timeslotLength;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return timeslotId == timeslot.timeslotId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeslotId, timeslotLength);
    }

    public int getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(int timeslotId) {
        this.timeslotId = timeslotId;
    }

    public int getTimeslotLength() {
        return timeslotLength;
    }

    public void setTimeslotLength(int timeslotLength) {
        this.timeslotLength = timeslotLength;
    }

    public int getTimeslotSeqnr() {
        return timeslotSeqnr;
    }

    public void setTimeslotSeqnr(int timeslotSeqnr) {
        this.timeslotSeqnr = timeslotSeqnr;
    }
}
