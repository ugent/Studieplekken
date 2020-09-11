package blok2.daos.db;

import blok2.daos.ITagsDao;
import blok2.helpers.Resources;
import blok2.model.LocationTag;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DBTagsDao extends DAO implements ITagsDao {

    /*****************************************************
     *   API calls for CRUD operations with public.TAGS  *
     *****************************************************/

    @Override
    public void addTag(LocationTag tag) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("add_tag"));
            st.setString(1, tag.getDutch());
            st.setString(2, tag.getEnglish());
            st.execute();
        }
    }

    @Override
    public void deleteTag(int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // First, delete the entries in LOCATION_TAGS that have a reference to 'tagId'
                removeTagUsageFromAllLocationsInTransaction(tagId, conn);

                // Then, delete the tag
                deleteTagInTransaction(tagId, conn);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void deleteTagInTransaction(int tagId, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("delete_tag"));
        st.setInt(1, tagId);
        st.execute();
    }

    @Override
    public void updateTag(LocationTag tag) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("update_tag"));
            st.setString(1, tag.getDutch());
            st.setString(2, tag.getEnglish());
            st.setInt(3, tag.getTagId());
            st.execute();
        }
    }

    @Override
    public void removeTagUsageFromAllLocations(LocationTag tag) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            removeTagUsageFromAllLocationsInTransaction(tag.getTagId(), conn);
        }
    }

    private void removeTagUsageFromAllLocationsInTransaction(int tagId, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("remove_locations_from_tag"));
        st.setInt(1, tagId);
        st.execute();
    }

    @Override
    public ArrayList<LocationTag> getTags() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("all_tags"));
            ResultSet rs = st.executeQuery();
            return createLocationTagList(rs);
        }
    }

    /**************************************************************
     *   API calls for CRUD operations with public.LOCATION_TAGS  *
     **************************************************************/

    @Override
    public LocationTag getTag(int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("get_tag"));
            st.setInt(1, tagId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return createLocationTag(rs);
            }
            return null;
        }
    }

    public List<LocationTag> getTagsOfLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            ResultSet rs = DBLocationDao.getTagsFromLocation(locationName, conn);
            return createLocationTagList(rs);
        }
    }

    /**
     * Assigning all LocationTags in the list "tags" to the location.
     * This is done by removing all tags from the location first, and then
     * adding the ones in the list.
     */
    @Override
    public void assignTagsToLocation(String locationName, List<LocationTag> tags) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // remove all tags from the location
                DBLocationDao.deleteTagsFromLocation(locationName, conn);

                // add entries to connect the location with the tags
                for (LocationTag tag : tags) {
                    DBLocationDao.insertTag(locationName, tag, conn);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static ArrayList<LocationTag> createLocationTagList(ResultSet rs) throws SQLException {
        ArrayList<LocationTag> tags = new ArrayList<>();
        while (rs.next()) {
            tags.add(createLocationTag(rs));
        }
        return tags;
    }

    public static LocationTag createLocationTag(ResultSet rs) throws SQLException {
        return new LocationTag(
                rs.getInt(Resources.databaseProperties.getString("tags_tag_id")),
                rs.getString(Resources.databaseProperties.getString("tags_dutch")),
                rs.getString(Resources.databaseProperties.getString("tags_english")));
    }
}
