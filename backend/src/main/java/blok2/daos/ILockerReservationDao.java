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
     * Gets a list of all locker reservations of a given location.
     */
    List<LockerReservation> getAllLockerReservationsOfLocation(int locationId,
                                                               boolean includePastReservations) throws SQLException;

    /**
     * Gets the locker reservation with the given details.
     */
    LockerReservation getLockerReservation(int locationId, int lockerNumber) throws SQLException;

    /**
     * Delete the locker reservation with the given details.
     */
    void deleteLockerReservation(int locationId, int lockerNumber) throws SQLException;

    /**
     * Add a locker reservation.
     */
    void addLockerReservation(LockerReservation lockerReservation) throws SQLException;

    /**
     * Change a locker reservation.
     */
    void changeLockerReservation(LockerReservation lockerReservation) throws SQLException;

}
