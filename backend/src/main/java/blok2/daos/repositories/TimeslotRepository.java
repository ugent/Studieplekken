package blok2.daos.repositories;

import blok2.model.calendar.Timeslot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Time;
import java.util.List;

public interface TimeslotRepository extends JpaRepository<Timeslot, Timeslot.TimeslotId> {
    List<Timeslot> getAllByLocationId(Integer locationId);

    @Query("SELECT t FROM Timeslot t where t.timeslotId.timeslotSequenceNumber = ?1")
    Timeslot getByTimeslotSeqnr(int sequence_number);

    @Query("delete FROM Timeslot t where t.timeslotId.timeslotSequenceNumber = ?1")
    void deleteTimeslotByTimeslotSeqnr(int sequence_number);
}
