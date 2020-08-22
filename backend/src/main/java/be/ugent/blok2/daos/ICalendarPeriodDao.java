package be.ugent.blok2.daos;

import be.ugent.blok2.model.calendar.CalendarPeriod;

import java.sql.SQLException;
import java.util.List;

public interface ICalendarPeriodDao extends IDao {
    List<CalendarPeriod> getCalendarPeriodsOfLocation(String locationName) throws SQLException;

    void addCalendarPeriods(List<CalendarPeriod> periods) throws SQLException;

    void updateCalendarPeriods(List<CalendarPeriod> from, List<CalendarPeriod> to) throws SQLException;

    void deleteCalendarPeriods(List<CalendarPeriod> periods) throws SQLException;
}
