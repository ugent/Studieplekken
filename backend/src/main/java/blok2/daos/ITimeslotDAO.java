package blok2.daos;

import blok2.model.calendar.Timeslot;

import java.util.List;

public interface ITimeslotDAO {

    List<Timeslot> getTimeslotsOfLocation(int locationId);

    Timeslot getTimeslot(int timeslotSeqNr);

    List<Timeslot> addTimeslots(List<Timeslot> timeslot);

    Timeslot addTimeslot(Timeslot timeslot);

    void deleteTimeslot(Timeslot timeslot);
}
