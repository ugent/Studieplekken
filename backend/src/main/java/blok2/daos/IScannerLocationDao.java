package blok2.daos;

import blok2.model.reservables.Location;
import blok2.model.users.User;

import java.sql.SQLException;
import java.util.List;

public interface IScannerLocationDao extends IDao {

    /**
     * Get the Users that are allowed to scan at the given location
     */
    List<User> getScannersOnLocation(int locationId) throws SQLException;

    /**
     * Get the Locations on which a given User is allowed to scan
     */
    List<Location> getLocationsToScanOfUser(String userId) throws SQLException;

    /**
     * Add a Location on which a User can scan
     */
    boolean addScannerLocation(int locationId, String userId) throws SQLException;

    /**
     * Delete a Location on which a User can scan
     */
    boolean deleteScannerLocation(int locationId, String userId) throws SQLException;

    /**
     * Remove scan-rights of all Users of a Location
     */
    boolean deleteAllScannersOfLocation(int locationId) throws SQLException;

    /**
     * Remove scan-rights on all locations of a User
     */
    boolean deleteAllLocationsOfScanner(String userId) throws SQLException;

    /**
     * Return whether the given User is allowed to scan at the given Location
     */
    boolean isUserAllowedToScan(String userId, int locationId) throws SQLException;

}
