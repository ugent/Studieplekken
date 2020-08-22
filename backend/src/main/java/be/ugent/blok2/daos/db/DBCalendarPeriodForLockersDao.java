package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.ICalendarPeriodForLockersDao;
import be.ugent.blok2.model.calendar.CalendarPeriodForLockers;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class DBCalendarPeriodForLockersDao extends ADB implements ICalendarPeriodForLockersDao {

    private final Logger logger = Logger.getLogger(DBCalendarPeriodForLockersDao.class.getSimpleName());

    @Override
    public List<CalendarPeriodForLockers> getCalendarPeriodsForLockersOfLocation(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                    .getString("get_calendar_periods_for_lockers_of_location"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            List<CalendarPeriodForLockers> periods = new ArrayList<>();

            while (rs.next()) {
                periods.add(createCalendarPeriod(rs));
            }

            return periods;
        }
    }

    @Override
    public void addCalendarPeriodsForLockers(List<CalendarPeriodForLockers> periods) throws SQLException {
        try (Connection conn = getConnection()) {
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
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
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

        try (Connection conn = getConnection()) {
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
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("update_calendar_period_for_lockers"));
        // set ...
        prepareCalendarPeriodForLockersPstmt(to, pstmt);
        // where ...
        prepareWhereClauseOfUpdatePstmt(from, pstmt);
        pstmt.execute();
    }

    @Override
    public void deleteCalendarPeriodsForLockers(List<CalendarPeriodForLockers> periods) throws SQLException {
        try (Connection conn = getConnection()) {
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
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties
                .getString("delete_calendar_period_for_lockers"));
        prepareCalendarPeriodForLockersPstmt(calendarPeriodForLockers, pstmt);
        pstmt.execute();
    }

    private CalendarPeriodForLockers createCalendarPeriod(ResultSet rs) throws SQLException {
        CalendarPeriodForLockers calendarPeriodForLockers = new CalendarPeriodForLockers();

        calendarPeriodForLockers.setStartsAt(rs.getString(databaseProperties
                .getString("calendar_period_for_lockers_starts_at")));
        calendarPeriodForLockers.setEndsAt(rs.getString(databaseProperties
                .getString("calendar_period_for_lockers_ends_at")));
        calendarPeriodForLockers.setReservableFrom(rs.getString(databaseProperties
                .getString("calendar_period_for_lockers_reservable_from")));

        calendarPeriodForLockers.setLocation(DBLocationDao.createLocation(rs));

        return calendarPeriodForLockers;
    }

    private void prepareCalendarPeriodForLockersPstmt(CalendarPeriodForLockers calendarPeriodForLockers,
                                                      PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, calendarPeriodForLockers.getLocation().getName());
        pstmt.setString(2, calendarPeriodForLockers.getStartsAt());
        pstmt.setString(3, calendarPeriodForLockers.getEndsAt());
        pstmt.setString(4, calendarPeriodForLockers.getReservableFrom());
    }

    private void prepareWhereClauseOfUpdatePstmt(CalendarPeriodForLockers calendarPeriodForLockers,
                                                 PreparedStatement pstmt) throws SQLException {
        pstmt.setString(5, calendarPeriodForLockers.getLocation().getName());
        pstmt.setString(6, calendarPeriodForLockers.getStartsAt());
        pstmt.setString(7, calendarPeriodForLockers.getEndsAt());
        pstmt.setString(8, calendarPeriodForLockers.getReservableFrom());
    }
}
