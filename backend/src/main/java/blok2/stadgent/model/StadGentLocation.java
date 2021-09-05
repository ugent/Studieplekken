package blok2.stadgent.model;

import blok2.daos.services.TimeslotService;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StadGentLocation {
    public static String baseUrl = "";

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("titel")
    private String name;

    @JsonProperty("teaser_img_url")
    private String teaserUrl;

    @JsonProperty("adres")
    private String adres;

    @JsonProperty("postcode")
    private String postcode = "";

    @JsonProperty("gemeente")
    private String gemeente = "";

    @JsonProperty("totale_capaciteit")
    private Integer capacity;

    @JsonProperty("gereserveede_plaatsen")
    private Integer reserved;

    private boolean isReservable;
    @JsonProperty("tag_1")
    public String getReservationMethod() {
        return isReservable ? "Reserveerbaar":"";
    }

    @JsonProperty("label_1")
    private String buildingName;

    @JsonProperty("lees_meer")
    public String getUrl() {
        return baseUrl + "dashboard/"+id;
    }

    @JsonProperty("openingsuren")
    public String hours;

    private Double lat;
    private Double lng;

    @JsonProperty("coordinates(x,y)")
    public String getCoordinates() {
        return lat + "," + lng;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTeaserUrl() {
        return teaserUrl;
    }

    public String getAdres() {
        return adres;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getGemeente() {
        return gemeente;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Integer getReserved() {
        return reserved;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public StadGentLocation(Integer id, String name, String teaserUrl, String adres, String postcode, String gemeente, Integer capacity, Integer reserved, boolean isReservable, String buildingName, String hours, Double lat, Double lng) {
        this.id = id;
        this.name = name;
        this.teaserUrl = teaserUrl;
        this.adres = adres;
        this.postcode = postcode;
        this.gemeente = gemeente;
        this.capacity = capacity;
        this.reserved = reserved;
        this.isReservable = isReservable;
        this.buildingName = buildingName;
        this.hours = hours;
        this.lat = lat;
        this.lng = lng;
    }

    public static StadGentLocation fromLocation(Location loc, TimeslotService ts) {
        Integer amountOfReservations = loc.getCurrentTimeslot() == null ? null:loc.getCurrentTimeslot().getAmountOfReservations();
        boolean isReservable = loc.getCurrentTimeslot() != null && loc.getCurrentTimeslot().isReservable();

        Stream<Timeslot> l = ts.getTimeslotsOfLocation(loc.getLocationId()).stream().filter(t -> t.timeslotDate().isEqual(LocalDate.now()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String hours = l.map(t -> t.getOpeningHour().format(formatter) + " - " + t.getClosingHour().format(formatter)).collect(Collectors.joining(","));
        LocalTime openingTime = loc.getCurrentTimeslot() == null ? null:loc.getCurrentTimeslot().getOpeningHour();
        LocalTime closingTime = loc.getCurrentTimeslot() == null ? null:loc.getCurrentTimeslot().getClosingHour();

        return new StadGentLocation(
                loc.getLocationId(),
                loc.getName(),
                loc.getImageUrl(),
                loc.getBuilding().getAddress(),
                "",
                "",
                loc.getNumberOfSeats(),
                amountOfReservations,
                isReservable,
                loc.getBuilding().getName(),
                hours,
                loc.getBuilding().getLatitude(),
                loc.getBuilding().getLongitude()
        );
    }
}