package blok2.model.reservations;

import blok2.model.calendar.Timeslot;
import blok2.model.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "location_reservations")
public class LocationReservation {

    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(
            name = "timeslotSequenceNumber",
            column = @Column(name = "timeslot_seqnr")
        ),
        @AttributeOverride(
            name = "timeslotDate",
            column = @Column(name = "timeslot_date")
        ),
        @AttributeOverride(
            name = "calendarId",
            column = @Column(name = "calendar_id")
        ),
        @AttributeOverride(
            name = "userId",
            column = @Column(name = "user_id")
        )
    })
    private LocationReservationId id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(
            name = "timeslot_seqnr", referencedColumnName = "timeslot_sequence_number",
            insertable = false, updatable = false),
        @JoinColumn(
            name = "timeslot_date", referencedColumnName = "timeslot_date",
            insertable = false, updatable = false),
        @JoinColumn(
            name = "calendar_id", referencedColumnName = "calendar_id",
            insertable = false, updatable = false)
    })
    private Timeslot timeslot;

    @Column(name = "attended")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Users can't set this themselves
    private Boolean attended;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LocationReservation() {
    }

    public LocationReservation(User user, Timeslot timeslot, Boolean attended) {
        this.id = new LocationReservationId(
                timeslot.getTimeslotSeqnr(),
                timeslot.getTimeslotDate(),
                timeslot.getCalendarId(),
                user.getUserId()
        );
        this.user = user;
        this.timeslot = timeslot;
        this.attended = attended;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public LocationReservationId getId() {
        return id;
    }

    public void setId(LocationReservationId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getAttended() {
        return attended;
    }

    @JsonIgnore
    public void setAttended(Boolean attended) {
        this.attended = attended;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    //</editor-fold>

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationReservation that = (LocationReservation) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(attended, that.attended) &&
                Objects.equals(timeslot, that.timeslot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, timeslot, attended);
    }

    public static class AttendedPostBody {
        boolean attended;

        public void setAttended(boolean attended) {
            this.attended = attended;
        }

        public boolean getAttended() {
            return this.attended;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Embeddable
    public static class LocationReservationId implements Serializable {
        public Integer timeslotSequenceNumber;
        public LocalDate timeslotDate;
        public Integer calendarId;
        public String userId;

        public LocationReservationId() {

        }

        public LocationReservationId(int timeslotSequenceNumber,
                                     LocalDate timeslotDate,
                                     int calendarId,
                                     String userId) {
            this.timeslotSequenceNumber = timeslotSequenceNumber;
            this.timeslotDate = timeslotDate;
            this.calendarId = calendarId;
            this.userId = userId;
        }

        @Override
        public String toString() {
            return String.format("[seq = '%d', date = '%s', calendarId = '%d', userId = '%s']",
                    timeslotSequenceNumber, timeslotDate.format(DateTimeFormatter.ISO_DATE), calendarId, userId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocationReservationId that = (LocationReservationId) o;
            return Objects.equals(timeslotSequenceNumber, that.timeslotSequenceNumber) &&
                    Objects.equals(timeslotDate, that.timeslotDate) &&
                    Objects.equals(calendarId, that.calendarId) &&
                    Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeslotSequenceNumber, timeslotDate, calendarId, userId);
        }
    }
    
}

