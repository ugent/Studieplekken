package be.ugent.blok2.helpers;

import be.ugent.blok2.helpers.date.CustomDate;

import java.util.ArrayList;
import java.util.List;

public class LocationReservationResponse {

    private List<CustomDate> valid;
    private List<CustomDate> full;

    public LocationReservationResponse() {
        this.valid = new ArrayList<>();
        this.full = new ArrayList<>();
    }

    public LocationReservationResponse(List<CustomDate> valid, List<CustomDate> full) {
        this.valid = valid;
        this.full = full;
    }

    public List<CustomDate> getValid() {
        return valid;
    }

    public List<CustomDate> getFull() {
        return full;
    }

    public void setValid(List<CustomDate> valid) {
        this.valid = valid;
    }

    public void setFull(List<CustomDate> full) {
        this.full = full;
    }
}