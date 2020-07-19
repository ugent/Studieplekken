package be.ugent.blok2.helpers.date;

/**
 * This is a 24h Timestamp
 */
public class Time implements Cloneable {
    private int hours;
    private int minutes;
    private int seconds;

    public Time() {
    }

    public Time(int hh, int mm, int ss) {
        hours = hh;
        minutes = mm;
        seconds = ss;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Time))
            return false;

        if (this == o)
            return true;

        Time t = (Time) o;
        return t.hours == hours
                && t.minutes == minutes
                && t.seconds == seconds;
    }

    @Override
    public int hashCode() {
        return (hours + ";" + minutes + ";" + seconds).hashCode();
    }

    @Override
    public Time clone() {
        try {
            return (Time) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
