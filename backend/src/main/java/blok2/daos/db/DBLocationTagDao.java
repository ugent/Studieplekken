package blok2.daos.db;

import blok2.daos.ILocationTagDao;
import blok2.helpers.Resources;
import blok2.model.LocationTag;
import blok2.model.reservables.Location;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DBLocationTagDao extends DAO implements ILocationTagDao {

    @Override
    public List<LocationTag> getTagsForLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            ResultSet rs = getTagsForLocation(locationName, conn);

            List<LocationTag> tags = new ArrayList<>();

            while (rs.next()) {
                tags.add(DBTagsDao.createLocationTag(rs));
            }

            return tags;
        }
    }

    public static ResultSet getTagsForLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_tags_for_location"));
        pstmt.setString(1, locationName);
        return pstmt.executeQuery();
    }

    @Override
    public List<Location> getLocationsForTag(int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            ResultSet rs = getLocationsForTag(tagId, conn);

            List<Location> locations = new ArrayList<>();

            while (rs.next()) {
                locations.add(DBLocationDao.createLocation(rs, conn));
            }

            return locations;
        }
    }

    public static ResultSet getLocationsForTag(int tagId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_locations_for_tag"));
        pstmt.setInt(1, tagId);
        return pstmt.executeQuery();
    }

    @Override
    public boolean addTagToLocation(String locationName, int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return addTagToLocation(locationName, tagId, conn);
        }
    }

    public static boolean addTagToLocation(String locationName, int tagId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("add_tag_to_location"));
        pstmt.setString(1, locationName);
        pstmt.setInt(2, tagId);
        return pstmt.executeUpdate() == 1;
    }

    @Override
    public boolean bulkAddTagsToLocation(String locationName, List<Integer> tagIds) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            boolean guard = true;
            for (int tagId : tagIds) {
                if (!addTagToLocation(locationName, tagId, conn)) {
                    guard = false;
                }
            }
            return guard;
        }
    }

    @Override
    public boolean bulkAddTagToLocations(List<String> locationNames, int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            boolean guard = true;
            for (String locationName : locationNames) {
                if (!addTagToLocation(locationName, tagId, conn)) {
                    guard = false;
                }
            }
            return guard;
        }
    }

    @Override
    public boolean deleteTagFromLocation(String locationName, int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_tag_from_location"));
            pstmt.setString(1, locationName);
            pstmt.setInt(2, tagId);
            return pstmt.executeUpdate() == 1;
        }
    }

    @Override
    public boolean deleteAllTagsFromLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_all_tags_from_location"));
            pstmt.setString(1, locationName);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean deleteAllTagsFromLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_all_tags_from_location"));
        pstmt.setString(1, locationName);
        return pstmt.executeUpdate() > 0;
    }

    @Override
    public boolean deleteTagFromAllLocations(int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return deleteTagFromAllLocations(tagId, conn);
        }
    }

    public static boolean deleteTagFromAllLocations(int tagId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_tag_from_all_locations"));
        pstmt.setInt(1, tagId);
        return pstmt.executeUpdate() > 0;
    }
}
