package blok2.daos;

import blok2.model.LocationTag;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface ITagsDao extends IDao {

    /*****************************************************
     *   API calls for CRUD operations with public.TAGS  *
     *****************************************************/

    void addTag(LocationTag tag) throws SQLException;

    void deleteTag(int tagId) throws SQLException;

    void updateTag(LocationTag tag) throws SQLException;

    void removeTagUsageFromAllLocations(LocationTag tag) throws SQLException;

    ArrayList<LocationTag> getTags() throws SQLException;

    LocationTag getTag(int tagId) throws SQLException;

    /**************************************************************
     *   API calls for CRUD operations with public.LOCATION_TAGS  *
     **************************************************************/

    List<LocationTag> getTagsOfLocation(String locationName) throws SQLException;

    void assignTagsToLocation(String locationName, List<LocationTag> tags) throws SQLException;
}
