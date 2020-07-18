package be.ugent.blok2.model.reservables;

import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;

import java.util.*;

public class Location implements Cloneable {
    private String name;
    private String address;
    private int numberOfSeats;
    private int numberOfLockers;
    private String mapsFrame; // this is the HTML-frame from Google Maps to put into the front-end
    private Map<Language, String> descriptions;
    private String imageUrl;
    private CustomDate startPeriodLockers;
    private CustomDate endPeriodLockers;

    public Location(String name, String address, int numberOfSeats, int numberOfLockers
            , String mapsFrame, Map<Language, String> descriptions, String imageUrl) {
        this.name = name;
        this.address = address;
        this.numberOfSeats = numberOfSeats;
        this.numberOfLockers = numberOfLockers;
        this.mapsFrame = mapsFrame;
        this.descriptions = descriptions;
        this.imageUrl = imageUrl;
    }

    // default constructor necessary for testing purposes
    public Location() {
        this.descriptions = new HashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return numberOfSeats == location.numberOfSeats &&
                numberOfLockers == location.numberOfLockers &&
                Objects.equals(name, location.name) &&
                Objects.equals(address, location.address) &&
                Objects.equals(mapsFrame, location.mapsFrame) &&
                Objects.equals(descriptions, location.descriptions) &&
                Objects.equals(imageUrl, location.imageUrl) &&
                Objects.equals(startPeriodLockers, location.startPeriodLockers) &&
                Objects.equals(endPeriodLockers, location.endPeriodLockers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }

    @Override
    public Location clone() {
        try {
            Location l = (Location) super.clone();

            l.setDescriptions(new HashMap<>());
            for (Language lang : descriptions.keySet()) {
                l.getDescriptions().put(lang, descriptions.get(lang));
            }

            l.setStartPeriodLockers(startPeriodLockers.clone());
            l.setEndPeriodLockers(endPeriodLockers.clone());

            return l;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public int getNumberOfLockers() {
        return numberOfLockers;
    }

    public String getMapsFrame() {
        return mapsFrame;
    }

    public Map<Language, String> getDescriptions() {
        return descriptions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public CustomDate getStartPeriodLockers() {
        return startPeriodLockers;
    }

    public CustomDate getEndPeriodLockers() {
        return endPeriodLockers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setMapsFrame(String mapsFrame) {
        this.mapsFrame = mapsFrame;
    }

    public void setDescriptions(Map<Language, String> descriptions) {
        this.descriptions = descriptions;
    }

    public void setImageUrl(String url) {
        this.imageUrl = url;
    }

    public void setStartPeriodLockers(CustomDate startPeriodLockers){
        this.startPeriodLockers = startPeriodLockers;
    }

    public void setEndPeriodLockers(CustomDate endPeriodLockers){
        this.endPeriodLockers = endPeriodLockers;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public void setNumberOfLockers(int numberOfLockers) {
        this.numberOfLockers = numberOfLockers;
    }

    //</editor-fold>
}
