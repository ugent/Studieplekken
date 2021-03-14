package blok2.daos;

import blok2.helpers.Pair;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import blok2.model.users.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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
     * Get a list of pairs which tell for each location what the next reservable from is.
     * The pair maps the location name to the reservable.
     */
    List<Pair<String, LocalDateTime>> getAllLocationNextReservableFroms() throws SQLException;

    /**
     * Adds a location
     */
    Location addLocation(Location location) throws SQLException;

    /**
     * Gets a location with the given name.
     */
    Location getLocationByName(String name) throws SQLException;

    /**
     * Gets a location with the given id
     */
    Location getLocationById(int locationId) throws SQLException;

    /**
     * Updates a location
     */
    void updateLocation(int locationId, Location location) throws SQLException;

    /**
     * Deletes a location
     */
    void deleteLocation(int locationId) throws SQLException;

    /**
     * Get all users that have volunteered for the specified location
     */
    List<User> getVolunteers(int locationId) throws SQLException;

    /**
     * Get all lockers of the specified location
     */
    List<Locker> getLockers(int locationId) throws SQLException;

    /**
     * Delete a locker
     */
    void deleteLocker(int locationId, int number) throws SQLException;

    /**
     * Approve or deny a new location
     */
    void approveLocation(Location location, boolean approval) throws SQLException;

    /**
     * Return all locations that are yet to be approved/denied
     */
    List<Location> getAllUnapprovedLocations() throws SQLException;

    /**
     * Return all locations that are yet to be approved/denied
     */
    void addVolunteer(int locationId, String userId) throws SQLException;

    /**
     * Return all locations that are yet to be approved/denied
     */
    void deleteVolunteer(int locationId, String userId) throws SQLException;

}
