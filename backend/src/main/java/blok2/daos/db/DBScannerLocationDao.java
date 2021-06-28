package blok2.daos.db;

import blok2.daos.IScannerLocationDao;
import blok2.helpers.Resources;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DBScannerLocationDao extends DAO implements IScannerLocationDao {

    @Override
    public List<User> getScannersOnLocation(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<User> users = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_scanners_of_location"));
            pstmt.setInt(1, locationId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(DBUserDao.createUser(rs, conn));
            }

            return users;
        }
    }

    @Override
    public List<Location> getLocationsToScanOfUser(String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Location> locations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_locations_of_scanner"));
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                locations.add(DBLocationDao.createLocation(rs,conn));
            }

            return locations;
        }
    }

    @Override
    public boolean addScannerLocation(int locationId, String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_scanner_on_location"));
            pstmt.setInt(1, locationId);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() == 1;
        }
    }

    @Override
    public boolean deleteScannerLocation(int locationId, String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_scanner_location"));
            pstmt.setInt(1, locationId);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() == 1;
        }
    }

    @Override
    public boolean deleteAllScannersOfLocation(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return deleteAllScannersOfLocation(locationId, conn);
        }
    }

    public static boolean deleteAllScannersOfLocation(int locationId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_scanners_of_location"));
        pstmt.setInt(1, locationId);
        return pstmt.executeUpdate() > 0;
    }

    @Override
    public boolean deleteAllLocationsOfScanner(String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return deleteAllLocationsOfScanner(userId, conn);
        }
    }

    public static boolean deleteAllLocationsOfScanner(String userId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locations_of_scanner"));
        pstmt.setString(1, userId);
        return pstmt.executeUpdate() > 0;
    }

    @Override
    public boolean isUserAllowedToScan(String userId, int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_scanner_on_location"));
            pstmt.setString(1, userId);
            pstmt.setInt(2, locationId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 1;
            }

            return false;
        }
    }

}
