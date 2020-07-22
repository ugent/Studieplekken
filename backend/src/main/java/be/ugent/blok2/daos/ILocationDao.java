package be.ugent.blok2.daos;

import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.DateFormatException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservables.Locker;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ILocationDao extends IDao {

    /**
     * Get a list of all available locations.
     */
    List<Location> getAllLocations() throws SQLException;

    /**
     * Adds a location.
     * Important note: this method should only add the location, its descriptions
     * in different languages and the lockers corresponding to the location
     * to the underlying database. Not the start/stop locker reservation dates
     * nor the calendar days. Other DAO methods handle these functionalities.
     */
    void addLocation(Location location) throws SQLException;

    /**
     * Gets a location with the given name.
     */
    Location getLocation(String name) throws SQLException;

    /**
     * Updates a location, name is the old name of the location.
     */
    void changeLocation(String name, Location location) throws SQLException;

    /**
     * Get all lockers of the specified location
     */
    Collection<Locker> getLockers(String locationName) throws SQLException;

    /**
     * This function will add 'count' lockers to the location with name 'locationName'
     * If count < 0, then count lockers will be removed. This is only possible if all
     * lockers are available, which means no locker in the location has been reserved.
     */
    void addLockers(String locationName, int count) throws SQLException;

    /**
     * This function will delete all lockers with a number higher than or equal to startNumber
     * of location with name 'locationName'.
     */
    void deleteLockers(String locationName, int startNumber) throws SQLException;

    /**
     * Deletes the location with the given name.
     */
    void deleteLocation(String name) throws SQLException;

    /**
     * Get all days on which the specified location will be opened to study
     */
    Collection<Day> getCalendarDays(String locationName) throws SQLException;

    /**
     * Deletes the all the calendar days between the given
     * start and end date for the given location.
     */
    void deleteCalendarDays(String name, String startdate, String enddate) throws SQLException;

    /**
     * Add all the days from the calendar object to the location with the given name.
     */
    void addCalendarDays(String name, Calendar calendar) throws SQLException;

    /**
     * Sets the list of user that are allowed to scan at the given location.
     */
    void setScannersForLocation(String name, List<User> sc) throws SQLException;

    /**
     * Gets a list of all users (their augentIDs) that are allowed to scan at the given location.
     */
    List<String> getScannersFromLocation(String name) throws SQLException;

    // TODO: CRUD operations for scanners at location

    /**
     * Get a map that maps location names to the number of reservations on the
     * given date.
     */
    Map<String, Integer> getCountOfReservations(CustomDate date) throws SQLException;
}
