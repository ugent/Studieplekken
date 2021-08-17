package blok2.daos.repositories;

import blok2.model.calendar.Timeslot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimeslotRepository extends JpaRepository<Timeslot, Timeslot.TimeslotId> {
    List<Timeslot> getAllByLocationId(Integer locationId);

    @Query("SELECT t FROM Timeslot t where t.timeslotSequenceNumber = ?1")
    Timeslot getByTimeslotSeqnr(int sequence_number);

    @Modifying
    @Transactional
    @Query("delete FROM Timeslot t where t.timeslotSequenceNumber = ?1")
    void deleteTimeslotByTimeslotSeqnr(int sequence_number);

    @Query("select t" +
            "   FROM Timeslot t " +
            "where t.locationId = ?1 " +
            "and (t.year > ?2 " +
            "or (t.year = ?2 and t.week > ?3)" +
            " or (t.year = ?2 and t.week = ?3 and t.dayOfWeek > ?4) " +
            "or (t.year = ?2 and t.week = ?3 and t.dayOfWeek = ?4 and t.closingHour > ?5))" +
            "order by t.year, t.week, t.dayOfWeek, t.closingHour")
    List<Timeslot> getCurrentOrNextTimeslot(int locationId, int year, int week, DayOfWeek value, LocalTime closingHour, Pageable pageable);
}
