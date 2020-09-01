package blok2.daos;

import blok2.helpers.date.CustomDate;
import blok2.model.reservations.LocationReservation;

import java.sql.SQLException;
import java.util.List;


public interface ILocationReservationDao extends IDao {
    List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws SQLException;

    List<LocationReservation> getAllLocationReservationsOfLocation(String name) throws SQLException;

    LocationReservation getLocationReservation(String augentID, CustomDate date) throws SQLException;

    void deleteLocationReservation(String augentID, CustomDate date) throws SQLException;

    void addLocationReservation(LocationReservation locationReservation) throws SQLException;

    LocationReservation scanStudent(String location, String barcode) throws SQLException;

    void setAllStudentsOfLocationToAttended(String location, CustomDate date) throws SQLException;

    int countReservedSeatsOfLocationOnDate(String location, CustomDate date) throws SQLException;

    List<LocationReservation> getAbsentStudents(String location, CustomDate date) throws SQLException;

    List<LocationReservation> getPresentStudents(String location, CustomDate date) throws SQLException;

    void setReservationToUnAttended(String augentId, CustomDate date) throws SQLException;
}
