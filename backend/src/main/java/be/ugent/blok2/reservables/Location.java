package be.ugent.blok2.reservables;

import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.model.users.User;


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

    private Collection<Day> calendar;
    private Collection<Locker> lockers;

    // the employees who have rights to scan at this location
    private Collection<User> scanners;

    public Location(String name, String address, int numberOfSeats, int numberOfLockers
            , String mapsFrame, Map<Language, String> descriptions, String imageUrl) {
        this.name = name;
        this.address = address;
        this.numberOfSeats = numberOfSeats;
        this.numberOfLockers = numberOfLockers;
        this.mapsFrame = mapsFrame;
        this.descriptions = descriptions;
        this.imageUrl = imageUrl;

        this.calendar = new ArrayList<>();
        this.lockers = new ArrayList<>();
        this.scanners = new ArrayList<>();
    }

    public Location(String name){
        this.name = name;
        this.numberOfLockers = 0;
        this.numberOfSeats = 0;
        this.calendar = new ArrayList<Day>();
        this.lockers = new ArrayList<Locker>();
        this.descriptions = new HashMap<>();
    }

    public Location(String name, String address, int numberOfSeats, int numberOfLockers
            , String mapsFrame, Map<Language, String> descriptions, String imageUrl
            , Collection<Day> calendar, Collection<Locker> lockers) {
        this(name, address, numberOfSeats, numberOfLockers, mapsFrame
                , descriptions, imageUrl);
        this.calendar = calendar;
        this.lockers = lockers;
    }

    // default constructor necessary for testing purposes
    public Location(){
        this.numberOfLockers = 0;
        this.numberOfSeats = 0;
        this.calendar = new ArrayList<>();
        this.lockers = new ArrayList<>();
        this.descriptions = new HashMap<>();
    }

    public void addLocker(int number, int id){
        int studentLimit = 2;
        Locker locker = new Locker(number, name, studentLimit);
        locker.setId(id);
        this.lockers.add(locker);
        this.numberOfLockers++;
    }

    public void deleteLockers(int startNumber){
        Collection<Locker> toRemove = new ArrayList<>();
        for (Locker locker : this.lockers) {
            if (locker.getNumber() >= startNumber) {
                toRemove.add(locker);
            }
        }
        for (Locker lock : toRemove) {
            this.lockers.remove(lock);
        }
        this.numberOfLockers -= toRemove.size();
    }

    public void addDay(Day day){
        this.calendar.add(day);
    }

    public void removeDay(Day day){
        this.calendar.remove(day);
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

    public Location locationWithoutScanners() {
        Location location = new Location(name, address, numberOfSeats, numberOfLockers, mapsFrame, descriptions,
                imageUrl, calendar, lockers);
        location.setStartPeriodLockers(startPeriodLockers);
        location.setEndPeriodLockers(endPeriodLockers);
        return location;
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

            l.setCalendar(new ArrayList<>());
            for (Day d : calendar) {
                l.getCalendar().add(d.clone());
            }

            l.setLockers(new ArrayList<>());
            for (Locker _l : lockers) {
                l.getLockers().add(_l.clone());
            }

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

    public Collection<Day> getCalendar(){
        return calendar;
    }

    public Collection<Locker> getLockers(){
        return lockers;
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

    public void setCalendar(Collection<Day> calendar){
        this.calendar = calendar;
    }

    public void setLockers(Collection<Locker> lockers){
        this.lockers = lockers;
    }

    public void setStartPeriodLockers(CustomDate startPeriodLockers){
        this.startPeriodLockers = startPeriodLockers;
    }

    public void setEndPeriodLockers(CustomDate endPeriodLockers){
        this.endPeriodLockers = endPeriodLockers;
    }

    public Collection<User> getScanners() { return scanners; }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public void setNumberOfLockers(int numberOfLockers) {
        this.numberOfLockers = numberOfLockers;
    }

    public void setScanners(Collection<User> scanners) { this.scanners = scanners; }



    //</editor-fold>
}
