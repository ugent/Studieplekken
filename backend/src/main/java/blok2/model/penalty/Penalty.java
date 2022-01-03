package blok2.model.penalty;

import blok2.helpers.Variables;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Objects;

@Entity
@Table(name = "penalty_points")
public class Penalty implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int penaltyId;

    @OneToOne()
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User designee;

    @Column(name = "user_id", nullable = false)
    private String user_id;

    @Column(name = "description")
    private String description;

    @OneToOne()
    @JoinColumn(name = "issuer_id")
    private User issuer;

    @Column(name = "class")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Users can't set this themselves
    private String penaltyClass = "custom";

    private int points;

    @Column(name="timeslot_sequence_number")
    @JsonIgnore
    private Integer timeslotSequenceNumber;

    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @CreatedDate
    @Column(name = "created_at", insertable = false)
    private LocalDateTime createdAt;

    @OneToOne()
    @JoinColumns(
            {
                    @JoinColumn(name = "timeslot_sequence_number", insertable = false, updatable = false),
                    @JoinColumn(name = "user_id", insertable = false, updatable = false),
            }
    )
    private LocationReservation reservation;

    public Penalty() {
    }

    public Penalty(String designee, int points, String description, User issuer, String penaltyClass) {
        this.user_id = designee;
        this.description = description;
        this.issuer = issuer;
        this.penaltyClass = penaltyClass;
        this.points = points;
    }

    public Penalty(int points, String description, User issuer, String penaltyClass, LocationReservation lr) {
        this(lr.getUser().getUserId(), points, description, issuer, penaltyClass);
        this.timeslotSequenceNumber = lr.getTimeslot().getTimeslotSeqnr();
    }


    public int getPenaltyId() {
        return penaltyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getPoints() {
        return points;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public User getIssuer() {
        return issuer;
    }

    public void setIssuer(User issuer) {
        this.issuer = issuer;
    }

    public int getTimeslotSequenceNumber() {
        return timeslotSequenceNumber;
    }

    public User getDesignee() {
        return designee;
    }
}
