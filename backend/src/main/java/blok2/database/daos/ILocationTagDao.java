package blok2.database.daos;

import blok2.model.LocationTag;
import blok2.model.reservables.Location;

import java.util.List;

public interface ILocationTagDao {

    /**
     * Get all existing tags
     */
    List<LocationTag> getAllLocationTags();

    /**
     * Get a specific tag
     */
    LocationTag getLocationTagById(int tagId);

    /**
     * List all the tags for a specific location
     */
    List<LocationTag> getTagsForLocation(int locationId);

    /**
     * List all the locations that have a specific tag
     */
    List<Location> getLocationsForTag(int tagId);

    /**
     * Add a new tag to the application database
     */
    LocationTag addLocationTag(LocationTag tag);

    /**
     * Delete a tag from the application database
     */
    void deleteLocationTag(int tagId);

    /**
     * Update a tag within the application database
     */
    void updateLocationTag(LocationTag tag);

    /**
     * Add a specific tag to a location
     */
    void addTagToLocation(int locationId, int tagId);

    /**
     * Add tags in bulk to a location
     */
    void bulkAddTagsToLocation(int locationId, List<Integer> tagIds);

    /**
     * Delete a specific tag from a location
     */
    void deleteTagFromLocation(int locationId, int tagId);

    /**
     * Delete all LocationTags for a specific location
     */
    void deleteAllTagsFromLocation(int locationId);

    /**
     * Remove a specific LocationTag from all the locations that use it.
     */
    void deleteTagFromAllLocations(int tagId);

}
