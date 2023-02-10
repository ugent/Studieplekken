package blok2.model.stats;

import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.persistence.*;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public class LocationOverviewStat {

    @Id
    private int locationId;

    private String locationName;

    private long reservationsTotal;

    @ElementCollection
    @MapKeyColumn(name = "HOI")
    @Column(name = "count")
    private Map<String, Long> reservationsTotalPerHOI;

    @ElementCollection
    @MapKeyColumn(name = "date")
    @Column(name = "count")
    private Map<String, Long> reservationsPerDay;

    @ElementCollection
    @MapKeyColumn(name = "date")
    @Column(name = "map")
    private Map<String, String> reservationsPerDayPerHOI;

    public LocationOverviewStat(int locationId, String locationName, long reservationsTotal, Map<String, Long> reservationsTotalPerHOI, Map<String, Long> reservationsPerDay, Map<String, Map<String, Long>> reservationsPerDayPerHOI) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.reservationsTotal = reservationsTotal;
        this.reservationsTotalPerHOI = reservationsTotalPerHOI;
        this.reservationsPerDay = reservationsPerDay;
        this.reservationsPerDayPerHOI = reservationsPerDayPerHOI.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new JSONObject(e.getValue()).toString()));
    }

    public LocationOverviewStat() {
    }

    public int getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public long getReservationsTotal() {
        return reservationsTotal;
    }

    public Map<String, Long> getReservationsTotalPerHOI() {
        return reservationsTotalPerHOI;
    }

    public Map<String, Long> getReservationsPerDay() {
        return reservationsPerDay;
    }

    public Map<String, String> getReservationsPerDayPerHOI() {
        return reservationsPerDayPerHOI;
    }
}

