package blok2.daos;

import blok2.model.calendar.CalendarPeriod;

import java.sql.SQLException;
import java.util.List;

public interface ICalendarPeriodDao extends IDao {
    List<CalendarPeriod> getCalendarPeriodsOfLocation(String locationName) throws SQLException;

    List<CalendarPeriod> getAllCalendarPeriods() throws SQLException;

    void addCalendarPeriods(List<CalendarPeriod> periods) throws SQLException;

    void updateCalendarPeriods(List<CalendarPeriod> from, List<CalendarPeriod> to) throws SQLException;

    void deleteCalendarPeriods(List<CalendarPeriod> periods) throws SQLException;

    CalendarPeriod getById(int calendarId) throws SQLException;

}
