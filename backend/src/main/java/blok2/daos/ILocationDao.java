package blok2.daos;

import blok2.model.reservables.Location;
import blok2.model.users.User;

import java.sql.SQLException;
import java.util.List;

public interface ILocationDao extends IDao {

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
     * Approve or deny a new location
     */
    void approveLocation(Location location, boolean approval) throws SQLException;

    /**
     * Return all locations that are yet to be approved/denied
     */
    List<Location> getAllUnapprovedLocations() throws SQLException;

     /**
     * Add a volunteer to a given location
     */
    void addVolunteer(int locationId, String userId) throws SQLException;

    /**
     * Delete a volunteer from a given location
     */
    void deleteVolunteer(int locationId, String userId) throws SQLException;

}
