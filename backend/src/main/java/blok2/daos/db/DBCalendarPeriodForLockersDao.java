package blok2.daos.db;

import blok2.daos.ICalendarPeriodForLockersDao;
import blok2.helpers.Resources;
import blok2.model.calendar.CalendarPeriodForLockers;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class DBCalendarPeriodForLockersDao extends DAO implements ICalendarPeriodForLockersDao {

    private final Logger logger = Logger.getLogger(DBCalendarPeriodForLockersDao.class.getSimpleName());

    @Override
    public List<CalendarPeriodForLockers> getCalendarPeriodsForLockersOfLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                    .getString("get_calendar_periods_for_lockers_of_location"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            List<CalendarPeriodForLockers> periods = new ArrayList<>();

            while (rs.next()) {
                periods.add(createCalendarPeriod(rs, conn));
            }

            return periods;
        }
    }

    @Override
    public void addCalendarPeriodsForLockers(List<CalendarPeriodForLockers> periods) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                for (CalendarPeriodForLockers calendarPeriodForLockers : periods) {
                    addCalendarPeriodForLockers(calendarPeriodForLockers, conn);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void addCalendarPeriodForLockers(CalendarPeriodForLockers calendarPeriodForLockers, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("insert_calendar_period_for_lockers"));
        prepareCalendarPeriodForLockersPstmt(calendarPeriodForLockers, pstmt);
        pstmt.execute();
    }

    @Override
    public void updateCalendarPeriodsForLockers(List<CalendarPeriodForLockers> from, List<CalendarPeriodForLockers> to)
            throws SQLException {
        if (from.size() != to.size()) {
            logger.warning("Update calendar periods for lockers: from-list has different sizing as opposed to the to-list");
            return;
        }

        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                for (int i = 0; i < from.size(); i++) {
                    updateCalendarPeriodForLockers(from.get(i), to.get(i), conn);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void updateCalendarPeriodForLockers(CalendarPeriodForLockers from, CalendarPeriodForLockers to
            , Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("update_calendar_period_for_lockers"));
        // set ...
        prepareCalendarPeriodForLockersPstmt(to, pstmt);
        // where ...
        prepareWhereClauseOfUpdatePstmt(from, pstmt);
        pstmt.execute();
    }

    @Override
    public void deleteCalendarPeriodsForLockers(List<CalendarPeriodForLockers> periods) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                for (CalendarPeriodForLockers calendarPeriodForLockers : periods) {
                    deleteCalendarPeriodForLockers(calendarPeriodForLockers, conn);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void deleteCalendarPeriodForLockers(CalendarPeriodForLockers calendarPeriodForLockers, Connection conn)
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties
                .getString("delete_calendar_period_for_lockers"));
        prepareCalendarPeriodForLockersPstmt(calendarPeriodForLockers, pstmt);
        pstmt.execute();
    }

    private CalendarPeriodForLockers createCalendarPeriod(ResultSet rs, Connection conn) throws SQLException {
        CalendarPeriodForLockers calendarPeriodForLockers = new CalendarPeriodForLockers();

        calendarPeriodForLockers.setStartsAt(rs.getDate(Resources.databaseProperties
                .getString("calendar_period_for_lockers_starts_at")).toLocalDate());
        calendarPeriodForLockers.setEndsAt(rs.getDate(Resources.databaseProperties
                .getString("calendar_period_for_lockers_ends_at")).toLocalDate());
        calendarPeriodForLockers.setReservableFrom(rs.getTimestamp(Resources.databaseProperties
                .getString("calendar_period_for_lockers_reservable_from")).toLocalDateTime());

        calendarPeriodForLockers.setLocation(DBLocationDao.createLocation(rs,conn));

        return calendarPeriodForLockers;
    }

    private void prepareCalendarPeriodForLockersPstmt(CalendarPeriodForLockers calendarPeriodForLockers,
                                                      PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, calendarPeriodForLockers.getLocation().getName());
        pstmt.setDate(2, Date.valueOf(calendarPeriodForLockers.getStartsAt()));
        pstmt.setDate(3, Date.valueOf(calendarPeriodForLockers.getEndsAt()));
        pstmt.setTimestamp(4, Timestamp.valueOf(calendarPeriodForLockers.getReservableFrom()));
    }

    private void prepareWhereClauseOfUpdatePstmt(CalendarPeriodForLockers calendarPeriodForLockers,
                                                 PreparedStatement pstmt) throws SQLException {
        pstmt.setString(5, calendarPeriodForLockers.getLocation().getName());
        pstmt.setDate(6, Date.valueOf(calendarPeriodForLockers.getStartsAt()));
        pstmt.setDate(7, Date.valueOf(calendarPeriodForLockers.getEndsAt()));
        pstmt.setTimestamp(8, Timestamp.valueOf(calendarPeriodForLockers.getReservableFrom()));
    }
}
