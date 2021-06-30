package blok2.daos.repositories;

import blok2.model.reservations.LocationReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LocationReservationRepository extends JpaRepository<LocationReservation, LocationReservation.LocationReservationId> {

    @Query("select lr from LocationReservation lr where lr.id.userId = ?1")
    List<LocationReservation> findAllByUserId(String userId);

    @Query("select lr from LocationReservation lr where lr.id.timeslotSequenceNumber = ?1 and lr.id.timeslotDate = ?2 and lr.id.calendarId = ?3")
    List<LocationReservation> findAllByTimeslot(int sequenceNumber, LocalDate date, int calendarId);

    @Query("select lr from LocationReservation lr where lr.id.timeslotDate = ?1 and lr.attended = false")
    List<LocationReservation> findAllUnattendedByDate(LocalDate date);

    @Query("select lr from LocationReservation lr where lr.id.timeslotSequenceNumber = ?1 and lr.id.timeslotDate = ?2 and lr.id.calendarId = ?3 and lr.attended is null")
    List<LocationReservation> findAllUnattendedByTimeslot(int sequenceNumber, LocalDate date, int calendarId);

    @Query("select lr from LocationReservation lr where lr.id.timeslotDate between ?1 and ?2")
    List<LocationReservation> findAllInWindowOfTime(LocalDate start, LocalDate end);

    @Query("select count(lr) from LocationReservation lr where lr.id.timeslotSequenceNumber = ?1 and lr.id.timeslotDate = ?2 and lr.id.calendarId = ?3")
    int countReservedSeatsOfTimeslot(int sequenceNumber, LocalDate date, int calendarId);

    @Query("select lr from LocationReservation lr " +
            "   join CalendarPeriod cp on cp.id = lr.id.calendarId " +
            "   join Location l on l.locationId = cp.location.locationId " +
            "where l.locationId = ?1")
    List<LocationReservation> getLocationReservationsAtLocationAtThisMoment(int locationId);

}
