package blok2.daos;

import blok2.model.reservations.LockerReservation;

import java.sql.SQLException;
import java.util.List;

public interface ILockerReservationDao extends IDao {

    /**
     * Gets a list of all locker reservations made by the user with the given augentID.
     */
    List<LockerReservation> getAllLockerReservationsOfUser(String augentID) throws SQLException;

    /**
     * Gets a list of all locker reservations made by the user with the given name.
     */
    List<LockerReservation> getAllLockerReservationsOfUserByName(String name) throws SQLException;

    /**
     * Gets a list of all locker reservations of a given location.
     */
    List<LockerReservation> getAllLockerReservationsOfLocation(String locationName,
                                                               boolean includePastReservations) throws SQLException;

    /**
     * Gets a list of all ongoing locker reservations of a given location.
     */
    List<LockerReservation> getAllLockerReservationsOfLocationWithoutKeyBroughtBack(String locationName) throws SQLException;

    /**
     * Gets the number of lockers that are used at the moment of a given location.
     */
    int getNumberOfLockersInUseOfLocation(String locationName) throws SQLException;

    /**
     * Gets the locker reservation with the given details.
     */
    LockerReservation getLockerReservation(String locationName, int lockerNumber) throws SQLException;

    /**
     * Delete the locker reservation with the given details.
     */
    void deleteLockerReservation(String locationName, int lockerNumber) throws SQLException;

    /**
     * Add a locker reservation.
     */
    void addLockerReservation(LockerReservation lockerReservation) throws SQLException;

    /**
     * Change a locker reservation.
     */
    void changeLockerReservation(LockerReservation lockerReservation) throws SQLException;

}
