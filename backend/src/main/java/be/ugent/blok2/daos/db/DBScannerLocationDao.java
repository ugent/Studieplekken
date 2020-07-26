package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.IScannerLocationDao;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.users.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Profile("db")
@Service
public class DBScannerLocationDao extends ADB implements IScannerLocationDao {
    @Override
    public List<User> getScannersOnLocation(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            List<User> users = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_scanners_of_location"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(DBAccountDao.createUser(rs));
            }

            return users;
        }
    }

    @Override
    public List<Location> getLocationsToScanOfUser(String augentid) throws SQLException {
        try (Connection conn = getConnection()) {
            List<Location> locations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_locations_of_scanner"));
            pstmt.setString(1, augentid);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                locations.add(DBLocationDao.createLocation(rs));
            }

            return locations;
        }
    }

    @Override
    public boolean addScannerLocation(String locationName, String augentid) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_scanner_on_location"));
            pstmt.setString(1, locationName);
            pstmt.setString(2, augentid);
            return pstmt.executeUpdate() == 1;
        }
    }

    @Override
    public boolean deleteScannerLocation(String locationName, String augentid) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_scanner_location"));
            pstmt.setString(1, locationName);
            pstmt.setString(2, augentid);
            return pstmt.executeUpdate() == 1;
        }
    }

    @Override
    public boolean deleteAllScannersOfLocation(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            return deleteAllScannersOfLocation(locationName, conn);
        }
    }

    public static boolean deleteAllScannersOfLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_scanners_of_location"));
        pstmt.setString(1, locationName);
        return pstmt.executeUpdate() > 0;
    }

    @Override
    public boolean deleteAllLocationsOfScanner(String augentid) throws SQLException {
        try (Connection conn = getConnection()) {
            return deleteAllLocationsOfScanner(augentid, conn);
        }
    }

    public static boolean deleteAllLocationsOfScanner(String augentid, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_locations_of_scanner"));
        pstmt.setString(1, augentid);
        return pstmt.executeUpdate() > 0;
    }

    @Override
    public boolean isUserAllowedToScan(String augentid, String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("count_scanner_on_location"));
            pstmt.setString(1, augentid);
            pstmt.setString(2, locationName);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 1;
            }

            return false;
        }
    }
}
