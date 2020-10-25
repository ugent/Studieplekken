package blok2.daos;

import blok2.helpers.date.CustomDate;
import blok2.model.calendar.Timeslot;
import blok2.model.reservations.LocationReservation;

import java.sql.SQLException;
import java.util.List;

public interface ILocationReservationDao extends IDao {
    /**
     * Get all locations where the name of the location equals 'locationName'.
     */
    /*
    List<LocationReservation> getAllLocationReservationsOfLocation(String locationName,
                                                                   boolean includePastReservations) throws SQLException;


    /**
     * start: YYYY-MM-DD
     *//*
    List<LocationReservation> getAllLocationReservationsOfLocationFrom(String locationName,
                                                                       String start,
                                                                       boolean includePastReservations) throws SQLException;

    /**
     * end: YYYY-MM-DD
     *//*
    List<LocationReservation> getAllLocationReservationsOfLocationUntil(String locationName,
                                                                        String end,
                                                                        boolean includePastReservations) throws SQLException;
    */
    /**
     * start: YYYY-MM-DD
     * end: YYYY-MM-DD
     */
    List<LocationReservation> getAllLocationReservationsOfLocationFromAndUntil(String locationName,
                                                                               String start,
                                                                               String end,
                                                                               boolean includePastReservations) throws SQLException;

    List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws SQLException;

    LocationReservation getLocationReservation(String augentID, Timeslot timeslot) throws SQLException;

    void deleteLocationReservation(String augentID, Timeslot timeslot) throws SQLException;

    void addLocationReservation(LocationReservation locationReservation) throws SQLException;

    LocationReservation scanStudent(String location, String barcode) throws SQLException;

    void setAllStudentsOfLocationToAttended(String location, CustomDate date) throws SQLException;

    long countReservedSeatsOfTimeslot(Timeslot timeslot) throws SQLException;

    List<LocationReservation> getAbsentStudents(String location, CustomDate date) throws SQLException;

    List<LocationReservation> getPresentStudents(String location, CustomDate date) throws SQLException;

    void setReservationToUnAttended(String augentId, CustomDate date) throws SQLException;

    List<LocationReservation> getAllLocationReservationsOfTimeslot(Timeslot timeslot) throws SQLException;

     boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException;
}
