package blok2.daos;

import blok2.model.LocationTag;
import blok2.model.reservables.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface ILocationTagDao extends IDao {
    /**
     * List all the tags for a specific location
     */
    ResultSet getTagsForLocation(String locationName) throws SQLException;

    /**
     * List all the locations that have a specific tag
     */
    ResultSet getLocationsForTag(int tagId) throws SQLException;

    /**
     * Add a specific tag to a location
     */
    boolean addTagToLocation(String locationName, int tagId) throws SQLException;

    /**
     * Add tags in bulk to a location
     */
    boolean bulkAddTagsToLocation(String locationName, List<Integer> tagIds) throws SQLException;

    /**
     * Add a tag to multiple locations
     */
    boolean bulkAddTagToLocations(List<String> locationNames, int tagId) throws SQLException;

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
