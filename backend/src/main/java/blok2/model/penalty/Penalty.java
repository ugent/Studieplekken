package blok2.model.penalty;

import blok2.helpers.Variables;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Objects;

public class Penalty implements Cloneable {
    private String augentID;
    private int eventCode;
    private LocalDate timestamp;
    private LocalDate reservationDate;
    private String reservationLocation;
    private int receivedPoints;
    private String remarks;

    public Penalty() {

    }

    public Penalty(String augentId, int eventCode, LocalDate timestamp, LocalDate reservationDate, String reservationLocation, int receivedPoints, String remarks) {
        this.augentID = augentId;
        this.eventCode = eventCode;
        this.timestamp = timestamp;
        this.reservationDate = reservationDate;
        this.reservationLocation = reservationLocation;
        this.receivedPoints = receivedPoints;
        this.remarks = remarks;
    }

    public static int calculateLateCancelPoints(LocalDateTime date, int points) {

        Calendar tdy = Calendar.getInstance();
        LocalDateTime today = LocalDateTime.of(tdy.get(Calendar.YEAR), tdy.get(Calendar.MONTH) + 1, tdy.get(Calendar.DAY_OF_MONTH),
                tdy.get(Calendar.HOUR_OF_DAY), tdy.get(Calendar.MINUTE), tdy.get(Calendar.SECOND));
        int secondsBetween = (int) ChronoUnit.SECONDS.between(today, date);

        LocalDateTime maxCancelDate = LocalDateTime.from(date);
        maxCancelDate = maxCancelDate.minusDays(Variables.maxCancelDate.getDayOfMonth());
        maxCancelDate = maxCancelDate.minusMonths(Variables.maxCancelDate.getMonthValue());
        maxCancelDate = maxCancelDate.minusYears(Variables.maxCancelDate.getYear());
        maxCancelDate = maxCancelDate.withHour(Variables.maxCancelDate.getHour());
        maxCancelDate = maxCancelDate.withMinute(Variables.maxCancelDate.getMinute());
        maxCancelDate = maxCancelDate.withSecond(Variables.maxCancelDate.getSecond());
        int maxCancelSeconds = (int) ChronoUnit.SECONDS.between(maxCancelDate, date);

        if (secondsBetween <= maxCancelSeconds) {
            return (int) Math.ceil((((double) (maxCancelSeconds - secondsBetween) / (double) (maxCancelSeconds)) * (double) points));
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Penalty penalty = (Penalty) o;
        return eventCode == penalty.eventCode &&
                augentID.equals(penalty.augentID) &&
                timestamp.equals(penalty.timestamp) &&
                reservationDate.equals(penalty.reservationDate) &&
                reservationLocation.equals(penalty.reservationLocation) &&
                receivedPoints == penalty.receivedPoints &&
                Objects.equals(remarks, penalty.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(augentID, eventCode, timestamp, reservationDate, reservationLocation, receivedPoints, remarks);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public String getAugentID() {
        return augentID;
    }

    public int getEventCode() {
        return eventCode;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public String getReservationLocation() {
        return reservationLocation;
    }

    public int getReceivedPoints() {
        return receivedPoints;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setAugentID(String augentID) {
        this.augentID = augentID;
    }

    public void setEventCode(int eventCode) {
        this.eventCode = eventCode;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public void setReservationLocation(String reservationLocation) {
        this.reservationLocation = reservationLocation;
    }

    public void setReceivedPoints(int receivedPoints) {
        this.receivedPoints = receivedPoints;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    //</editor-fold>
}
