package blok2.daos.repositories;

import blok2.model.calendar.Timeslot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimeslotRepository extends JpaRepository<Timeslot, Integer> {
    List<Timeslot> getAllByLocationId(Integer locationId);

    @Query("SELECT t FROM Timeslot t where t.locationId = ?1 and t.timeslotDate > ?2")
    List<Timeslot> getAllByLocationIdAndAfterTimeslotDate(Integer locationId, LocalDate timeslotDate);

    @Query("SELECT t FROM Timeslot t where t.timeslotSequenceNumber = ?1")
    Timeslot getByTimeslotSeqnr(int sequence_number);

    @Modifying
    @Transactional
    @Query("delete FROM Timeslot t where t.timeslotSequenceNumber = ?1")
    void deleteTimeslotByTimeslotSeqnr(int sequence_number);

    @Query("select t" +
            "   FROM Timeslot t " +
            "where t.locationId = ?1 " +
            "and (t.timeslotDate > ?2 " +
            "or (t.timeslotDate = ?2 and t.closingHour > ?3))" +
            "order by t.timeslotDate, t.closingHour")
    List<Timeslot> getCurrentOrNextTimeslot(int locationId, LocalDate date, LocalTime closingHour, Pageable pageable);
}
