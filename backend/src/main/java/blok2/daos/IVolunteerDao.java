package blok2.daos;

import blok2.model.users.User;

import java.sql.SQLException;
import java.util.List;

public interface IVolunteerDao extends IDao {
    
    /**
     * Get all users that have volunteered for the specified location
     */
    List<User> getVolunteers(int locationId) throws SQLException;

    /**
     * Add a volunteer to a given location
     */
    void addVolunteer(int locationId, String userId) throws SQLException;

    /**
     * Delete a volunteer from a given location
     */
    void deleteVolunteer(int locationId, String userId) throws SQLException;

}
