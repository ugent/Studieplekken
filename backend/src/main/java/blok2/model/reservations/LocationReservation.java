package blok2.model.reservations;

import blok2.model.calendar.Timeslot;
import blok2.model.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.PreparedStatement;
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
            column = @Column(name = "timeslot_sequence_number")
        ),
        @AttributeOverride(
            name = "userId",
            column = @Column(name = "user_id")
        )
    })
    private LocationReservationId id;
    
    @Column(name = "state")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Users can't set this themselves
    // NOTE(ydndonck): String instead of state enum, because postgres tries to save it as bytea instead of text.
    private String state = State.APPROVED.name();

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(
        name = "timeslot_sequence_number", referencedColumnName = "sequence_number",
        insertable = false, updatable = false)
    private Timeslot timeslot;

    /*@Column(name = "attended")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Users can't set this themselves
    private Boolean attended;*/

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LocationReservation() {
    }

    /*public LocationReservation(User user, Timeslot timeslot, Boolean attended) {
        this.id = new LocationReservationId(
                timeslot.getTimeslotSeqnr(),
                user.getUserId()
        );
        this.user = user;
        this.timeslot = timeslot;
        this.attended = attended;
    }*/
    
    public LocationReservation(User user, Timeslot timeslot, State state) {
        this.id = new LocationReservationId(
                timeslot.getTimeslotSeqnr(),
                user.getUserId()
        );
        this.user = user;
        this.timeslot = timeslot;
        this.setState(state != null? state : State.APPROVED);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public LocationReservationId getId() {
        return id;
    }

    public void setId(LocationReservationId id) {
        this.id = id;
    }
    
    public String getState() {
        return state;
    }
    
    @JsonIgnore
    public State getStateE() {
        return State.valueOf(state);
    }

    @JsonIgnore
    public void setState(State state) {
        this.setState(state == null? null :state.name());
    }
    
    @JsonIgnore
    public void setState(String state) {
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /*public Boolean getAttended() {
        return attended;
    }

    @JsonIgnore
    public void setAttended(Boolean attended) {
        this.attended = attended;
    }*/

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
                Objects.equals(state, that.state) &&
                Objects.equals(timeslot, that.timeslot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, timeslot, state);
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
    
    public static enum State {
        PENDING,
        REJECTED,
        APPROVED,
        PRESENT,
        ABSENT;
    }
    
    @Embeddable
    public static class LocationReservationId implements Serializable {
        public Integer timeslotSequenceNumber;
        public String userId;

        public LocationReservationId() {

        }

        public LocationReservationId(int timeslotSequenceNumber,
                                     String userId) {
            this.timeslotSequenceNumber = timeslotSequenceNumber;
            this.userId = userId;
        }

        @Override
        public String toString() {
            return String.format("[seq = '%d', userId = '%s']",
                    timeslotSequenceNumber, userId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocationReservationId that = (LocationReservationId) o;
            return Objects.equals(timeslotSequenceNumber, that.timeslotSequenceNumber) &&
                    Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeslotSequenceNumber,  userId);
        }
    }
    
}

