package blok2.model.stats;

import blok2.extensions.orm.CustomLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class LocationStat {

    @Id
    private int locationId;

    private String locationName;

    private boolean open;

    private boolean reservable;

    private int numberOfSeats;

    private int numberOfTakenSeats;

    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timeslotDate;

    public LocationStat(int locationId, String locationName, boolean open, boolean reservable, int numberOfSeats, int numberOfTakenSeats, LocalDateTime timeslotDate) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.open = open;
        this.reservable = reservable;
        this.numberOfSeats = numberOfSeats;
        this.numberOfTakenSeats = numberOfTakenSeats;
        this.timeslotDate = timeslotDate;
    }

    public LocationStat() {
    }

    public int getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isReservable() {
        return reservable;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public int getNumberOfTakenSeats() {
        return numberOfTakenSeats;
    }

    public LocalDateTime getTimeslotDate() {
        return timeslotDate;
    }
}
