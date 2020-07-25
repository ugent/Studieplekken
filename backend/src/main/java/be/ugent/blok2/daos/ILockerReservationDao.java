package be.ugent.blok2.daos;

import be.ugent.blok2.model.reservations.LockerReservation;

import java.sql.SQLException;
import java.util.List;

public interface ILockerReservationDao extends IDao {
    /**
     * Gets a list of all lockerreservations made by the user with the given augentID.
     */
    List<LockerReservation> getAllLockerReservationsOfUser(String augentID) throws SQLException;
    /**
     * Gets a list of all lockerreservations made by the user with the given name.
     */
    List<LockerReservation> getAllLockerReservationsOfUserByName(String name) throws SQLException;
    /**
     * Gets a list of all lockerreservations of a given location.
     */
    List<LockerReservation> getAllLockerReservationsOfLocation(String name) throws SQLException;
    /**
     * Gets a list of all ongoing lockerreservations of a given location.
     */
    List<LockerReservation> getAllLockerReservationsOfLocationWithoutKeyBroughtBack(String name) throws SQLException;

    /**
     * Gets the number of lockers that are used at the moment of a given location.
     */
    int getNumberOfLockersInUseOfLocation(String name) throws SQLException;

    /**
     * Gets the lockerreservation with the given details.
     */
    LockerReservation getLockerReservation(String locationName, int lockerNumber) throws SQLException;

    /**
     * Delete the lockerreservation with the given details.
     */
    void deleteLockerReservation(String locationName, int lockerNumber) throws SQLException;

    /**
     * Add a lockerreservation.
     */
    void addLockerReservation(LockerReservation lockerReservation) throws SQLException;

    /**
     * Change a lockerreservation.
     */
    void changeLockerReservation(LockerReservation lockerReservation) throws SQLException;
}
