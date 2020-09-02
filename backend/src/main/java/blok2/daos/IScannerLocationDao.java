package blok2.daos;

import blok2.model.reservables.Location;
import blok2.model.users.User;

import java.sql.SQLException;
import java.util.List;

public interface IScannerLocationDao {
    /**
     * Get the Users that are allowed to scan at the given location
     */
    List<User> getScannersOnLocation(String locationName) throws SQLException;

    /**
     * Get the Locations on which a given User is allowed to scan
     */
    List<Location> getLocationsToScanOfUser(String augentid) throws SQLException;

    /**
     * Add a Location on which a User can scan
     */
    boolean addScannerLocation(String locationName, String augentid) throws SQLException;

    /**
     * Delete a Location on which a User can scan
     */
    boolean deleteScannerLocation(String locationName, String augentid) throws SQLException;

    /**
     * Remove scan-rights of all Users of a Location
     */
    boolean deleteAllScannersOfLocation(String locationName) throws SQLException;

    /**
     * Remove scan-rights on all locations of a User
     */
    boolean deleteAllLocationsOfScanner(String augentid) throws SQLException;

    /**
     * Return whether the given User is allowed to scan at the given Location
     */
    boolean isUserAllowedToScan(String augentid, String locationName) throws SQLException;
}
