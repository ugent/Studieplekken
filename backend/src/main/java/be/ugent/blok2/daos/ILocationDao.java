package be.ugent.blok2.daos;

import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservables.Locker;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Note that all add/update/delete operations on lockers will happen in cascade
 * upon add/update/delete operations on a location if the number of lockers change
 */
public interface ILocationDao extends IDao {

    /**
     * Get a list of all available locations.
     */
    List<Location> getAllLocations() throws SQLException;

    /**
     * Adds a location
     */
    void addLocation(Location location) throws SQLException;

    /**
     * Gets a location with the given name.
     */
    Location getLocation(String name) throws SQLException;

    /**
     * Updates a location, name is the old name of the location.
     */
    void updateLocation(String locationName, Location location) throws SQLException;

    /**
     * Deletes the location with the given name.
     */
    void deleteLocation(String locationName) throws SQLException;

    /**
     * Get all lockers of the specified location
     */
    Collection<Locker> getLockers(String locationName) throws SQLException;

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
     * Get a map that maps location names to the number of reservations on the
     * given date.
     */
    Map<String, Integer> getCountOfReservations(CustomDate date) throws SQLException;
}
