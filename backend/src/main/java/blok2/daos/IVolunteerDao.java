package blok2.daos;

import blok2.model.reservables.Location;
import blok2.model.users.User;

import java.sql.SQLException;
import java.util.List;

public interface IVolunteerDao {
    
    /**
     * Get all users that have volunteered for the specified location
     */
    List<User> getVolunteers(int locationId) throws SQLException;

    /**
     * Get all locations for which the specified user has volunteered
     */
    List<Location> getVolunteeredLocations(String userId) throws SQLException;

    /**
     * Add a volunteer to a given location
     */
    void addVolunteer(int locationId, String userId) throws SQLException;

    /**
     * Delete a volunteer from a given location
     */
    void deleteVolunteer(int locationId, String userId) throws SQLException;

}
