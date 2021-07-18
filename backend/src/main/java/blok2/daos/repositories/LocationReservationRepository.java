package blok2.daos.repositories;

import blok2.model.reservations.LocationReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public interface LocationReservationRepository extends JpaRepository<LocationReservation, LocationReservation.LocationReservationId> {

    @Query("select lr from LocationReservation lr where lr.id.userId = ?1")
    List<LocationReservation> findAllByUserId(String userId);

    @Query("select lr from LocationReservation lr where lr.id.timeslotSequenceNumber = ?1")
    List<LocationReservation> findAllByTimeslot(int sequenceNumber);

    @Query("select lr from LocationReservation lr " +
            "join Timeslot t on lr.id.timeslotSequenceNumber = t.timeslotId.timeslotSequenceNumber " +
            "where t.year = ?1 and t.week = ?2 and t.dayOfWeek = ?3 and lr.attended = false")
    List<LocationReservation> findAllUnattendedByDate(int year, int week, DayOfWeek value);

    @Query("select lr from LocationReservation lr where lr.id.timeslotSequenceNumber = ?1 and lr.attended is null")
    List<LocationReservation> findAllUnknownAttendanceByTimeslot(int sequenceNumber);

    @Query("select count(lr) from LocationReservation lr where lr.id.timeslotSequenceNumber = ?1")
    int countReservedSeatsOfTimeslot(int sequenceNumber);

    @Query("select lr from LocationReservation lr " +
            "   join Timeslot t on lr.id.timeslotSequenceNumber = t.timeslotId.timeslotSequenceNumber " +
            "where t.locationId = ?1")
    List<LocationReservation> getLocationReservationsAtLocationAtThisMoment(int locationId);


    @Query("select lr from LocationReservation lr " +
            "   join Timeslot t on lr.id.timeslotSequenceNumber = t.timeslotId.timeslotSequenceNumber " +
            "where t.locationId = ?1 " +
            "and t.year > ?2 " +
            "or (t.year = ?2 and t.week > ?3)" +
            " or (t.year = ?2 and t.week = ?3 and t.dayOfWeek > ?4)")
    List<LocationReservation> findAllByLocationIdAndDateAfter(int locationId, int year, int week, DayOfWeek value);


    // TODO fix this method
    @Query("select lr from LocationReservation lr " +
            "   join Timeslot t on lr.id.timeslotSequenceNumber = t.timeslotId.timeslotSequenceNumber " +
            "where t.locationId = ?1 and t.year = ?1 and t.week = ?2 and t.dayOfWeek = ?3")
    List<LocationReservation> findAllInWindowOfTime(int startYear, int startWeek, int startDay, int endYear, int endWeek, int endDay);
}
