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

    @Override
    public void addTag(LocationTag tag) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("add_tag"));
            st.setString(1, tag.getDutch());
            st.setString(2, tag.getEnglish());
            st.execute();
        }
    }

    public static void addTag(LocationTag tag, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("add_tag"));
        st.setString(1, tag.getDutch());
        st.setString(2, tag.getEnglish());
        st.execute();
    }

    @Override
    public void deleteTag(int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("delete_tag"));
            st.setInt(1, tagId);
            st.execute();
        }
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
    public List<LocationTag> getTags() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("all_tags"));
            ResultSet rs = st.executeQuery();
            return createLocationTagList(rs);
        }
    }

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