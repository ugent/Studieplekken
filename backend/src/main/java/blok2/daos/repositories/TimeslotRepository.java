package blok2.daos.repositories;

import blok2.model.calendar.Timeslot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Time;
import java.util.List;

public interface TimeslotRepository extends JpaRepository<Timeslot, Timeslot.TimeslotId> {
    List<Timeslot> getAllByLocationId(Integer locationId);

    Timeslot getByTimeslotSeqnr(int timeslotSeqnr);

    void deleteTimeslotByTimeslotSeqnr(int timeslotSeqnr);
}
