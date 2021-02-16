package blok2.daos;

import blok2.model.LocationTag;
import blok2.model.reservables.Location;

import java.sql.SQLException;
import java.util.List;

public interface ILocationTagDao extends IDao {

    /**
     * List all the tags for a specific location
     */
    List<LocationTag> getTagsForLocation(int locationId) throws SQLException;

    /**
     * List all the locations that have a specific tag
     */
    List<Location> getLocationsForTag(int tagId) throws SQLException;

    /**
     * Add a specific tag to a location
     */
    boolean addTagToLocation(int locationId, int tagId) throws SQLException;

    /**
     * Add tags in bulk to a location
     */
    boolean bulkAddTagsToLocation(int locationId, List<Integer> tagIds) throws SQLException;

    /**
     * Delete a specific tag from a location
     */
    boolean deleteTagFromLocation(int locationId, int tagId) throws SQLException;

    /**
     * Delete all LocationTags for a specific location
     */
    boolean deleteAllTagsFromLocation(int locationId) throws SQLException;

    /**
     * Remove a specific LocationTag from all the locations that use it.
     */
    boolean deleteTagFromAllLocations(int tagId) throws SQLException;

}
