package blok2.daos;

import blok2.model.calendar.CalendarPeriodForLockers;

import java.sql.SQLException;
import java.util.List;

public interface ICalendarPeriodForLockersDao extends IDao {
    List<CalendarPeriodForLockers> getCalendarPeriodsForLockersOfLocation(String locationName) throws SQLException;

    void addCalendarPeriodsForLockers(List<CalendarPeriodForLockers> periods) throws SQLException;

    void updateCalendarPeriodsForLockers(List<CalendarPeriodForLockers> from, List<CalendarPeriodForLockers> to) throws SQLException;

    void deleteCalendarPeriodsForLockers(List<CalendarPeriodForLockers> periods) throws SQLException;
}
