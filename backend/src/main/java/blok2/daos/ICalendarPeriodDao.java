package blok2.daos;

import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.model.calendar.CalendarPeriod;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ICalendarPeriodDao extends IDao {

    /**
     * Get all calendar periods of a specific location
     */
    List<CalendarPeriod> getCalendarPeriodsOfLocation(int getLocationId) throws SQLException;

    /**
     * Get all calendar periods within the week that starts with the specified date
     */
    List<CalendarPeriod> getCalendarPeriodsInWeek(LocalDate firstDayOfWeek) throws SQLException;

    /**
     * Get all calendar periods in the period that starts and ends with the specified dates
     */
    List<CalendarPeriod> getCalendarPeriodsInPeriod(LocalDate start, LocalDate end) throws SQLException;

    /**
     * Get all calendar periods
     */
    List<CalendarPeriod> getAllCalendarPeriods() throws SQLException;

    /**
     * Add a calendar period
     */
    void addCalendarPeriods(List<CalendarPeriod> periods) throws SQLException;

    /**
     * Update a calendar period
     */
    void updateCalendarPeriod(CalendarPeriod period) throws SQLException;

    /**
     * Update multiple calendar periods
     */
    void updateCalendarPeriods(List<CalendarPeriod> from, List<CalendarPeriod> to) throws SQLException;

    /**
     * Delete a calendar period
     */
    void deleteCalendarPeriod(CalendarPeriod calendarPeriod) throws SQLException;

    /**
     * Get the status of a calendar period
     */
    Pair<LocationStatus, String> getStatus(int locationId) throws SQLException;

    /**
     * Get a calendar period by its id
     */
    CalendarPeriod getById(int calendarId) throws SQLException;

}
