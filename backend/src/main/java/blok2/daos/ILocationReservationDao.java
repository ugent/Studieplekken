package blok2.daos;

import blok2.helpers.Pair;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ILocationReservationDao extends IDao {

    List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws SQLException;

    List<Pair<LocationReservation, CalendarPeriod>> getAllLocationReservationsAndCalendarPeriodsOfUser(String userId) throws SQLException;

    LocationReservation getLocationReservation(String augentID, Timeslot timeslot) throws SQLException;

    boolean deleteLocationReservation(String augentID, Timeslot timeslot) throws SQLException;

    /**
     * This method should only be used for testing purposes, this does not check if there is still
     * room for reservations. You should use addLocationReservationIfStillRoomAtomically() instead.
     */
    @Deprecated
    void addLocationReservation(LocationReservation locationReservation) throws SQLException;

    LocationReservation scanStudent(String location, String barcode) throws SQLException;

    void setAllStudentsOfLocationToAttended(String location, LocalDate date) throws SQLException;

    long countReservedSeatsOfTimeslot(Timeslot timeslot) throws SQLException;

    List<LocationReservation> getAbsentStudents(String location, LocalDate date) throws SQLException;

    List<LocationReservation> getPresentStudents(String location, LocalDate date) throws SQLException;

    void setReservationAttendance(String augentId, Timeslot timeslot, boolean attendance) throws SQLException;

    List<LocationReservation> getAllLocationReservationsOfTimeslot(Timeslot timeslot) throws SQLException;

     boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException;

     int amountOfReservationsRightNow(String location) throws SQLException;
}
