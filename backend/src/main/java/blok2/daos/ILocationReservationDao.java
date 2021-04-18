package blok2.daos;

import blok2.helpers.Pair;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;

import java.sql.SQLException;
import java.util.List;

public interface ILocationReservationDao extends IDao {

    /**
     * Get all location reservations of the specified user
     */
    List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws SQLException;

    /**
     * Get the location reservation of a specified user at a specified timeslot
     */
    LocationReservation getLocationReservation(String augentID, Timeslot timeslot) throws SQLException;

    /**
     * Delete the location reservation of a specified user at a specified timeslot
     */
    boolean deleteLocationReservation(String augentID, Timeslot timeslot) throws SQLException;

    /**
     * This method should only be used for testing purposes, this does not check if there is still
     * room for reservations. You should use addLocationReservationIfStillRoomAtomically() instead.
     */
    @Deprecated
    void addLocationReservation(LocationReservation locationReservation) throws SQLException;

    /**
     * Count the number of reserved seats at a specified timeslot
     */
    long countReservedSeatsOfTimeslot(Timeslot timeslot) throws SQLException;

    /**
     * Set the attendance for a location reservation of a specified user at a specified timeslot
     */
    boolean setReservationAttendance(String augentId, Timeslot timeslot, boolean attendance) throws SQLException;


    /**
     * Get all location reservations at a specified timeslot
     */
    List<LocationReservation> getAllLocationReservationsOfTimeslot(Timeslot timeslot) throws SQLException;

    /**
     * Try to make a location reservation while making sure that the maximum capacity of the location is not exceeded
     */
     boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException;

    /**
     * Get the number of location reservation of a specified location at this moment in time
     */
     int amountOfReservationsRightNow(int locationId) throws SQLException;
}
