package be.ugent.blok2.daos;

import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.reservations.LocationReservation;

import be.ugent.blok2.helpers.date.CustomDate;

import java.util.List;


public interface ILocationReservationDao extends IDao {
    List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws NoSuchUserException;

    List<LocationReservation> getAllLocationReservationsOfUserByName(String userName);

    List<LocationReservation> getAllLocationReservationsOfLocation(String name);

    LocationReservation getLocationReservation(String augentID, CustomDate date);

    void deleteLocationReservation(String augentID, CustomDate date);

    LocationReservation addLocationReservation(LocationReservation locationReservation);

    LocationReservation scanStudent(String location, String barcode);

    void setAllStudentsOfLocationToAttended(String location, CustomDate date);

    int countReservedSeatsOfLocationOnDate(String location, CustomDate date);

    List<LocationReservation> getAbsentStudents(String location, CustomDate date);

    List<LocationReservation> getPresentStudents(String location, CustomDate date);

    void setReservationToUnAttended(String augentId, CustomDate date);
}
