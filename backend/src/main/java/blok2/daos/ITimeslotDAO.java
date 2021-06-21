package blok2.daos;

import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import org.threeten.extra.YearWeek;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ITimeslotDAO extends IDao {

    List<Timeslot> getTimeslotsOfLocation(int locationId) throws SQLException;

    Timeslot getTimeslot(int timeslotSeqNr) throws SQLException;

    List<Timeslot> addTimeslots(List<Timeslot> timeslot) throws SQLException;

    Timeslot addTimeslot(Timeslot timeslot) throws SQLException;

    void deleteTimeslot(Timeslot timeslot) throws SQLException;
}
