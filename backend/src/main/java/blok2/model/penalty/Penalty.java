package blok2.model.penalty;

import blok2.helpers.Variables;
import blok2.model.reservables.Location;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Objects;

@Entity
@Table(name = "penalty_book")
public class Penalty implements Cloneable {

    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(
            name = "userId",
            column = @Column(name = "user_id")
        ),
        @AttributeOverride(
            name = "eventCode",
            column = @Column(name = "event_code")
        ),
        @AttributeOverride(
            name = "timestamp",
            column = @Column(name = "timestamp")
        )
    })
    private PenaltyId penaltyId;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @ManyToOne
    @JoinColumn(name = "reservation_location_id")
    private Location reservationLocation;

    @Column(name = "received_points")
    private int receivedPoints;

    @Column(name = "remarks")
    private String remarks;

    public Penalty() {

    }

    public Penalty(String userId, int eventCode, LocalDateTime timestamp, LocalDate reservationDate, Location reservationLocation, int receivedPoints, String remarks) {
        this.penaltyId = new PenaltyId(userId, eventCode, timestamp);
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
        return Objects.equals(penaltyId.eventCode, penalty.penaltyId.eventCode) &&
                Objects.equals(penaltyId.userId, penalty.penaltyId.userId) &&
                Objects.equals(penaltyId.timestamp.withNano(0), penalty.penaltyId.timestamp.withNano(0)) && // withNano(0) to avoid loss of accuracy during database save problem on Windows.
                Objects.equals(reservationDate, penalty.reservationDate) &&
                Objects.equals(reservationLocation, penalty.reservationLocation) &&
                receivedPoints == penalty.receivedPoints &&
                Objects.equals(remarks, penalty.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(penaltyId.userId, penaltyId.eventCode, penaltyId.timestamp.withNano(0), reservationDate, reservationLocation, receivedPoints, remarks);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public PenaltyId getPenaltyId() {
        return penaltyId;
    }

    public void setPenaltyId(PenaltyId penaltyId) {
        this.penaltyId = penaltyId;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Location getReservationLocation() {
        return reservationLocation;
    }

    public void setReservationLocation(Location reservationLocation) {
        this.reservationLocation = reservationLocation;
    }

    public int getReceivedPoints() {
        return receivedPoints;
    }

    public void setReceivedPoints(int receivedPoints) {
        this.receivedPoints = receivedPoints;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    //</editor-fold>

    @Embeddable
    public static class PenaltyId implements Serializable {
        public String userId;
        public Integer eventCode;
        public LocalDateTime timestamp;

        public PenaltyId() {

        }

        public PenaltyId(String userId, Integer eventCode, LocalDateTime timestamp) {
            this.userId = userId;
            this.eventCode = eventCode;
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PenaltyId penaltyId = (PenaltyId) o;
            return Objects.equals(userId, penaltyId.userId) &&
                    Objects.equals(eventCode, penaltyId.eventCode) &&
                    Objects.equals(timestamp.withNano(0), penaltyId.timestamp.withNano(0));
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, eventCode, timestamp.withNano(0));
        }
    }

}
