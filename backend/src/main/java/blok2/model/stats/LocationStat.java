package blok2.model.stats;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class LocationStat {

    @Id
    private int locationId;

    private String locationName;

    private boolean open;

    private boolean reservable;

    private int numberOfSeats;

    private int numberOfTakenSeats;

    public LocationStat(int locationId, String locationName, boolean open, boolean reservable, int numberOfSeats, int numberOfTakenSeats) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.open = open;
        this.reservable = reservable;
        this.numberOfSeats = numberOfSeats;
        this.numberOfTakenSeats = numberOfTakenSeats;
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
}
