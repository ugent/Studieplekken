package blok2.helpers.date;

import blok2.helpers.exceptions.DateFormatException;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

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
        return this.toDateString() + " " + toTimeString();
    }

    public String toDateString() {
        return String.format("%04d", year) + "-" + String.format("%02d", month) + "-"
                + String.format("%02d", day);
    }

    public String toTimeString() {
        return String.format("%02d", hrs) + ":" + String.format("%02d", min)
                + ":" + String.format("%02d", sec);
    }

    public String toTimeWithoutSecondsString() {
        return String.format("%02d", hrs) + ":" + String.format("%02d", min);
    }

    /**
     * Parsing following formats into a CustomDate:
     *   YYYY-MM-DD HH24:MI:SS
     *   YYYY-MM-DDTHH24:MI:SS
     *   YYYY-MM-DD
     *
     * Note on last format: the values of hrs, min, sec will all be 0.
     */
    public static CustomDate parseString(String s) throws DateFormatException {
        if (s == null || s.isEmpty()) {
            return null;
        }

        String[] strings;
        if (s.contains("T")) {
            strings = s.split("T");
        } else if (s.contains(" ")) {
            strings = s.split(" ");
        } else {
            strings = s.split("-");
            if (strings.length != 3)
                return null;

            return new CustomDate(Integer.parseInt(strings[0]),
                    Integer.parseInt(strings[1]),
                    Integer.parseInt(strings[2]));
        }

        String[] dateParts = strings[0].split("-");
        String[] timeParts = strings[1].split(":");

        if (dateParts.length != 3) {
            throw new DateFormatException("Date should have a day, a month and a year.");
        }

        return new CustomDate(Integer.parseInt(dateParts[0]),
                Integer.parseInt(dateParts[1]),
                Integer.parseInt(dateParts[2]),
                Integer.parseInt(timeParts[0]),
                Integer.parseInt(timeParts[1]),
                Integer.parseInt((timeParts[2].split("\\."))[0]));
    }

    public static CustomDate today() {
        java.util.Calendar juc = java.util.Calendar.getInstance();
        return new CustomDate(juc.get(Calendar.YEAR),
                juc.get(Calendar.MONTH) + 1, juc.get(Calendar.DATE));
    }

    public static CustomDate now() {
        java.util.Calendar juc = java.util.Calendar.getInstance();
        return new CustomDate(juc.get(Calendar.YEAR), juc.get(Calendar.MONTH) + 1, juc.get(Calendar.DATE)
                , juc.get(Calendar.HOUR_OF_DAY), juc.get(Calendar.MINUTE), juc.get(Calendar.SECOND));
    }

    @JsonIgnore
    public boolean isToday() {
        LocalDate localDate = LocalDate.now();
        return year == localDate.getYear() && day == localDate.getDayOfMonth() && month == localDate.getMonthValue();
    }

    @JsonIgnore
    public boolean isSameDay(CustomDate day) {
        return day.year == this.year && day.month == this.month && day.day == this.day;
    }

    public static Date toDate(CustomDate customDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            return format.parse(customDate.toString().replace('T', ' '));
        } catch (ParseException e) {
            return null;
        }
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
        return date.year == this.year && date.month == this.month && date.day == this.day && date.hrs == this.hrs &&
                date.min == this.min && date.sec == this.sec;
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
