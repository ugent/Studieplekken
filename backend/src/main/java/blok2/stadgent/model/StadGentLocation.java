package blok2.stadgent.model;

import blok2.model.reservables.Location;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StadGentLocation {
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
        return isReservable ? "pc":"";
    }

    @JsonProperty("label_1")
    private String buildingName;

    @JsonProperty("lees_meer")
    public String getUrl() {
        return "";
    }

    private LocalTime openingHour;
    private LocalTime closingHour;

    @JsonProperty("openingsuren_vanaf")
    public String getOpeningHour() {
        if(closingHour == null)
            return "";

        return openingHour.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @JsonProperty("openingsuren_tot")
    public String geClosingHour() {
        if(closingHour == null)
            return "";
        return closingHour.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

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

    public StadGentLocation(Integer id, String name, String teaserUrl, String adres, String postcode, String gemeente, Integer capacity, Integer reserved, boolean isReservable, String buildingName, LocalTime openingHour, LocalTime closingHour, Double lat, Double lng) {
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
        this.closingHour = closingHour;
        this.openingHour = openingHour;
        this.lat = lat;
        this.lng = lng;
    }

    public static StadGentLocation fromLocation(Location loc) {
        Integer amountOfReservations = loc.getCurrentTimeslot() == null ? null:loc.getCurrentTimeslot().getAmountOfReservations();
        boolean isReservable = loc.getCurrentTimeslot() != null && loc.getCurrentTimeslot().isReservable();

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
                openingTime,
                closingTime,
                loc.getBuilding().getLatitude(),
                loc.getBuilding().getLongitude()
        );
    }
}