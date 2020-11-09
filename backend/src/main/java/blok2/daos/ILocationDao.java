package blok2.daos;

import blok2.helpers.date.CustomDate;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;

import java.sql.Connection;
import java.sql.SQLException;
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
    List<Locker> getLockers(String locationName) throws SQLException;

    /**
     * Delete a locker
     */
    void deleteLocker(String locationName, int number) throws SQLException;

    /**
     * Get a map that maps location names to the number of reservations on the
     * given date.
     */
    Map<String, Integer> getCountOfReservations(CustomDate date) throws SQLException;
}
