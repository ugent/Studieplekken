package be.ugent.blok2.helpers.date;

import java.util.Objects;

public class Day implements Cloneable {
    private CustomDate date;
    private Time openingHour;
    private Time closingHour;
    private CustomDate openForReservationDate;

    public Day() {
    }

    public Day(CustomDate date, Time openingHour, Time closingHour, CustomDate openForReservationDate) {
        this.date = date;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.openForReservationDate = openForReservationDate;
    }

    public CustomDate getDate() {
        return date;
    }

    public Time getOpeningHour() {
        return openingHour;
    }

    public Time getClosingHour() {
        return closingHour;
    }

    public CustomDate getOpenForReservationDate() {
        return openForReservationDate;
    }

    public void setDate(CustomDate date) {
        this.date = date;
    }

    public void setOpeningHour(Time openingHour) {
        this.openingHour = openingHour;
    }

    public void setClosingHour(Time closingHour) {
        this.closingHour = closingHour;
    }

    public void setOpenForReservationDate(CustomDate openForReservationDate) {
        this.openForReservationDate = openForReservationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Day day = (Day) o;
        return Objects.equals(date, day.date) &&
                Objects.equals(openingHour, day.openingHour) &&
                Objects.equals(closingHour, day.closingHour) &&
                Objects.equals(openForReservationDate, day.openForReservationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, openingHour, closingHour, openForReservationDate);
    }

    @Override
    public Day clone() {
        Day d = new Day();
        d.setDate(date.clone());
        d.setOpeningHour(openingHour.clone());
        d.setClosingHour(closingHour.clone());
        d.setOpenForReservationDate(openForReservationDate.clone());
        return d;
    }
}
