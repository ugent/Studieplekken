package be.ugent.blok2.daos;

import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.DateFormatException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservables.Locker;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface ILocationDao {

    /**
     * Get a list of all available locations.
     */
    List<Location> getAllLocations();

    /**
     * Get a list of all available locations but without their lockers and calendar.
     */
    List<Location> getAllLocationsWithoutLockersAndCalendar();

    /**
     * Get a list of all available locations but without their lockers.
     */
    List<Location> getAllLocationsWithoutLockers();

    /**
     * Get a list of all available locations but without their calendar.
     */
    List<Location> getAllLocationsWithoutCalendar();

    /**
     * Adds a location to the list of all locations.
     */
    Location addLocation(Location location) throws AlreadyExistsException;

    /**
     * Gets a location with the given name.
     */
    Location getLocation(String name);

    /**
     * Gets a location with the given name without the calendar.
     */
    Location getLocationWithoutCalendar(String name);

    /**
     * Gets a location with the given name without lockers.
     */
    Location getLocationWithoutLockers(String name);

    /**
     * Gets a location with the given name without the calendar and lockers.
     */
    Location getLocationWithoutLockersAndCalendar(String name);

    /**
     * Updates a location, name is the old name of the location.
     */
    void changeLocation(String name, Location location);

    /**
     * This function will add 'count' lockers to the location with name 'locationName'
     * and the numbers of the lockers will count up from startNumber
     */
    void addLockers(String locationName, int count, int startNumber);

    /**
     * This function will delete all lockers with a number higher than or equal to startNumber
     * of location with name 'locationName'.
     */
    void deleteLockers(String locationName, int startNumber);

    /**
     * Deletes the location with the given name.
     */
    void deleteLocation(String name);

    /**
     * Deletes the all the calendar days between the given
     * start and end date for the given location.
     */
    void deleteCalendarDays(String name, String startdate, String enddate) throws DateFormatException;

    /**
     * Add all the days from the calendar object to the location with the given name.
     */
    void addCalendarDays(String name, Calendar calendar);

    /**
     * Sets the list of user that are allowed to scan at the given location.
     */
    void setScannersForLocation(String name, List<User> sc);

    /**
     * Gets a list of all users (their name) that are allowed to scan at the given locatino.
     */
    List<String> getScannersFromLocation(String name);

    /**
     * Get a map that maps location names to the number of reservations on the
     * given date.
     */
    Map<String, Integer> getCountOfReservations(CustomDate date);
}
