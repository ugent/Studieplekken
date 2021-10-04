package blok2.daos;

import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ILocationReservationDao {

    /**
     * Get all location reservations of the specified user
     */
    List<LocationReservation> getAllLocationReservationsOfUser(String userId);

    /*
     * Get the location reservation of a specified user at a specified timeslot
     */
    LocationReservation getLocationReservation(String userId, Timeslot timeslot);

    /**
     * Get all LocationReservations and corresponding CalendarPeriods of unattended reservations
     */
    List<LocationReservation> getUnattendedLocationReservations(LocalDate date) ;

    /**
     * Get all LocationReservations and corresponding CalendarPeriods of unattended reservations
     * It starts at 21PM of the previous day until 21PM of the given date.
     */
    List<LocationReservation> getUnattendedLocationReservationsWith21PMRestriction(LocalDate date);

    /**
     * Get all users that have made a reservation within the window of time that is provided through the parameters.
     * Note: the window includes 'start' but does not include 'end': window = [start, end)
     */
    List<User> getUsersWithReservationForWindowOfTime(LocalDate start, LocalDate end);

    /**
     * Get all location reservations at a specified timeslot
     */
    List<LocationReservation> getAllLocationReservationsOfTimeslot(Timeslot timeslot);

    /**
     * Count the number of reserved seats at a specified timeslot
     */
    long countReservedSeatsOfTimeslot(Timeslot timeslot);

    /**
     * Delete the location reservation of a specified user at a specified timeslot
     */
    void deleteLocationReservation(LocationReservation locationReservation);

    /**
     * This method should only be used for testing purposes, this does not check if there is still
     * room for reservations. You should use addLocationReservationIfStillRoomAtomically() instead.
     */
    @Deprecated
    LocationReservation addLocationReservation(LocationReservation locationReservation);

    /**
     * Set the attendance for a location reservation of a specified user at a specified timeslot
     */
    boolean setReservationAttendance(String userId, Timeslot timeslot, boolean attendance);

    /**
     * Try to make a location reservation while making sure that the maximum capacity of the location is not exceeded
     */
    boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException;

    /**
     * Set all LocationReservations corresponding to the given Timeslot for which the field attended is null
     * to false. This sets the not scanned students to unattended for the given timeslot.
     */
    void setNotScannedStudentsToUnattended(Timeslot timeslot);

    /**
     * Get all location reservations of the specified location
     */
    List<LocationReservation> getAllFutureLocationReservationsOfLocation(int locationId);

}
