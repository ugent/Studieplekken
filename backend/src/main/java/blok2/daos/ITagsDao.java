package blok2.daos;

import blok2.model.LocationTag;

import java.sql.SQLException;
import java.util.List;

public interface ITagsDao extends IDao {

    /**
     * Add a new tag to the application database
     */
    void addTag(LocationTag tag) throws SQLException;

    /**
     * Delete a tag from the application database
     */
    void deleteTag(int tagId) throws SQLException;

    /**
     * Update a tag within the application database
     */
    void updateTag(LocationTag tag) throws SQLException;

    /**
     * Get all existing tags
     */
    List<LocationTag> getTags() throws SQLException;

    /**
     * Get a specific tag
     */
    LocationTag getTag(int tagId) throws SQLException;

}
