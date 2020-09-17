package blok2.daos;

import blok2.model.LocationTag;
import blok2.model.reservables.Location;

import java.sql.SQLException;
import java.util.List;

public interface ILocationTagDao extends IDao {

    /**
     * List all the tags for a specific location
     */
    List<LocationTag> getTagsForLocation(String locationName) throws SQLException;

    /**
     * List all assigned tags for a specific location
     */
    List<LocationTag> getAssignedTagsForLocation(String locationName) throws SQLException;

    /**
     * List all the locations that have a specific tag
     */
    List<Location> getLocationsForTag(int tagId) throws SQLException;

    /**
     * Add a specific tag to a location
     */
    boolean addTagToLocation(String locationName, int tagId) throws SQLException;

    /**
     * Assign all specified tags to a location, and un-assign all tags of that
     * location that aren't mentioned.
     */
    boolean assignTagsToLocation(String locationName, List<LocationTag> tags) throws SQLException;

    /**
     * Delete a specific tag from a location
     */
    boolean deleteTagFromLocation(String locationName, int tagId) throws SQLException;

    /**
     * Delete all LocationTags for a specific location
     */
    boolean deleteAllTagsFromLocation(String locationName) throws SQLException;

    /**
     * Remove a specific LocationTag from all the locations that use it.
     */
    boolean deleteTagFromAllLocations(int tagId) throws SQLException;

}
