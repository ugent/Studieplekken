package blok2.daos;

import blok2.model.reservables.Location;
import blok2.model.users.User;

import java.util.List;

public interface IVolunteerDao {
    
    /**
     * Get all users that have volunteered for the specified location
     */
    List<User> getVolunteers(int locationId);

    /**
     * Get all locations for which the specified user has volunteered
     */
    List<Location> getVolunteeredLocations(String userId);

    /**
     * Add a volunteer to a given location
     */
    void addVolunteer(int locationId, String userId);

    /**
     * Delete a volunteer from a given location
     */
    void deleteVolunteer(int locationId, String userId);

}
