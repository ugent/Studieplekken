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
    public List<LocationTag> getTagsForLocation(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            ResultSet rs = getTagsForLocation(locationId, conn);

            List<LocationTag> tags = new ArrayList<>();

            while (rs.next()) {
                tags.add(DBTagsDao.createLocationTag(rs));
            }

            return tags;
        }
    }

    public static ResultSet getTagsForLocation(int locationId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_tags_for_location"));
        pstmt.setInt(1, locationId);
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
    public boolean addTagToLocation(int locationId, int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return addTagToLocation(locationId, tagId, conn);
        }
    }

    public static boolean addTagToLocation(int locationId, int tagId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("add_tag_to_location"));
        pstmt.setInt(1, locationId);
        pstmt.setInt(2, tagId);
        return pstmt.executeUpdate() == 1;
    }

    @Override
    public boolean bulkAddTagsToLocation(int locationId, List<Integer> tagIds) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            boolean guard = true;
            for (int tagId : tagIds) {
                if (!addTagToLocation(locationId, tagId, conn)) {
                    guard = false;
                }
            }
            return guard;
        }
    }

    @Override
    public boolean deleteTagFromLocation(int locationId, int tagId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_tag_from_location"));
            pstmt.setInt(1, locationId);
            pstmt.setInt(2, tagId);
            return pstmt.executeUpdate() == 1;
        }
    }

    @Override
    public boolean deleteAllTagsFromLocation(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_all_tags_from_location"));
            pstmt.setInt(1, locationId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean deleteAllTagsFromLocation(int locationId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_all_tags_from_location"));
        pstmt.setInt(1, locationId);
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
