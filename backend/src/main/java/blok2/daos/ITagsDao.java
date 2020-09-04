package blok2.daos;

import blok2.model.LocationTag;

import java.sql.SQLException;
import java.util.ArrayList;

public interface ITagsDao extends IDao {
    void addTag(LocationTag tag) throws SQLException;

    void deleteTag(int tagId) throws SQLException;

    void updateTag(LocationTag tag) throws SQLException;

    void removeTagUsageFromAllLocations(LocationTag tag) throws SQLException;

    ArrayList<LocationTag> getTags() throws SQLException;

    LocationTag getTag(int tagId) throws SQLException;
}
