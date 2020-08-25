package be.ugent.blok2.model.calendar;

import java.util.Objects;

public class Period {
    private String startsAt; // date: YYYY-MM-DD
    private String endsAt; // date: YYYY-MM-DD

    @Override
    public String toString() {
        return "Period{" +
                "startsAt='" + startsAt + '\'' +
                ", endsAt='" + endsAt + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Period period = (Period) o;
        return Objects.equals(startsAt, period.startsAt) &&
                Objects.equals(endsAt, period.endsAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startsAt, endsAt);
    }

    public String getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(String startsAt) {
        this.startsAt = startsAt;
    }

    public String getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(String endsAt) {
        this.endsAt = endsAt;
    }
}
