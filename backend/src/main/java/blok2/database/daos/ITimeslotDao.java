package blok2.database.daos;

import blok2.model.calendar.Timeslot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ITimeslotDao {

    List<Timeslot> getTimeslotsOfLocation(int locationId);

    List<Timeslot> getTimeslotsOfLocationAfterTimeslotDate(int locationId, LocalDate timeslotDate);

    List<Timeslot> getTimeslotsOfLocationOnTimeslotDate(int locationId, LocalDate timeslotDate);

    Timeslot getTimeslot(int timeslotSeqNr);

    List<Timeslot> addTimeslots(List<Timeslot> timeslot);

    Timeslot addTimeslot(Timeslot timeslot);

    void deleteTimeslot(Timeslot timeslot);

    Timeslot updateTimeslot(Timeslot timeslot);

    Optional<Timeslot> getCurrentOrNextTimeslot(int locationId);
}
