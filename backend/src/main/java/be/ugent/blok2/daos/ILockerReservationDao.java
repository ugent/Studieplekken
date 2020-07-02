package be.ugent.blok2.daos;

import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.reservations.LockerReservation;

import java.util.List;

public interface ILockerReservationDao {
    /**
     * Gets a list of all lockerreservations made by the user with the given augentID.
     */
    List<LockerReservation> getAllLockerReservationsOfUser(String augentID) throws NoSuchUserException;
    /**
     * Gets a list of all lockerreservations made by the user with the given name.
     */
    List<LockerReservation> getAllLockerReservationsOfUserByName(String name);
    /**
     * Gets a list of all lockerreservations of a given location.
     */
    List<LockerReservation> getAllLockerReservationsOfLocation(String name);
    /**
     * Gets a list of all ongoing lockerreservations of a given location.
     */
    List<LockerReservation> getAllLockerReservationsOfLocationWithoutKeyBroughtBack(String name);

    /**
     * Gets the number of lockers that are used at the moment of a given location.
     */
    int getNumberOfLockersInUseOfLocation(String name);

    /**
     * Gets the lockerreservation with the given details.
     */
    LockerReservation getLockerReservation(String augentID, int lockerID, CustomDate startDate, CustomDate endDate);

    /**
     * Delete the lockerreservation with the given details.
     */
    void deleteLockerReservation(String augentID, int LockerID, CustomDate startDate, CustomDate endDate);

    /**
     * Add a lockerreservation.
     */
    LockerReservation addLockerReservation(LockerReservation lockerReservation);

    /**
     * Change a lockerreservation.
     */
    void changeLockerReservation(LockerReservation lockerReservation);
}
