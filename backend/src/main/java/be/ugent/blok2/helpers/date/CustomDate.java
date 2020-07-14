package be.ugent.blok2.helpers.date;

import be.ugent.blok2.helpers.exceptions.DateFormatException;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.ParseException;
import java.time.LocalDate;

/**
 * This is a custom class to represent a date.
 */
public class CustomDate implements Cloneable {

    private int year, month, day, hrs, min, sec;

    public CustomDate() {
    }

    public CustomDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public CustomDate(int year, int month, int day, int hrs, int min, int sec) {
        this(year, month, day);
        this.hrs = hrs;
        this.min = min;
        this.sec = sec;
    }

    @Override
    public String toString() {
        return String.format("%04d", year) + "-" + String.format("%02d", month) + "-"
                + String.format("%02d", day) + "T" + String.format("%02d", hrs)
                + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec);
    }

    public static CustomDate parseString(String s) throws DateFormatException {
        if (s == null || s.isEmpty()) {
            return null;
        }

        String[] strings = s.split("T");
        String[] dateParts = strings[0].split("-");
        String[] timeParts = strings[1].split(":");

        if(dateParts.length != 3){
            throw new DateFormatException("Date should have a day, a month and a year.");
        }

        return new CustomDate(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]), Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]), Integer.parseInt((timeParts[2].split("\\."))[0]));
    }

    @JsonIgnore
    public boolean isToday() {
        LocalDate localDate = LocalDate.now();
        return year == localDate.getYear() && day == localDate.getDayOfMonth() && month == localDate.getMonthValue();
    }

    @JsonIgnore
    public boolean isSameDay(CustomDate day){
        return  day.year == this.year && day.month == this.month && day.day == this.day;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        /* Check if o is an instance of CustomDate or not
          "null instanceof [type]" also returns false */
        if (!(obj instanceof CustomDate)) {
            return false;
        }

        // typecast o to CustomDate so that we can compare data members
        CustomDate date = (CustomDate) obj;

        // Compare the data members and return accordingly
        if (date.year != this.year || date.month != this.month || date.day != this.day || date.hrs != this.hrs ||
                date.min != this.min || date.sec != this.sec) {
            return false;
        }
        return true;
    }

    public CustomDate clone() {
        try {
            return (CustomDate) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHrs() {
        return hrs;
    }

    public void setHrs(int hrs) {
        this.hrs = hrs;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }
    //</editor-fold>
}
