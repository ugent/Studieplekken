package blok2.model.reservations;

import blok2.model.calendar.Timeslot;
import blok2.model.users.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Objects;


public class LocationReservation {
    @Valid
    private User user;
    private LocalDateTime createdAt;
    @Valid
    private Timeslot timeslot;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Users can't set this themselves
    private Boolean attended;

    public LocationReservation() {
    }

    public LocationReservation(User user, LocalDateTime createdAt, Timeslot timeslot, Boolean attended) {
        this.user = user;
        this.createdAt = createdAt;
        this.timeslot = timeslot;
        this.attended = attended;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    //</editor-fold>



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationReservation that = (LocationReservation) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(attended, that.attended) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(timeslot, that.timeslot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, createdAt, timeslot);
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
}

