package blok2.daos.db;

import blok2.daos.ICalendarPeriodDao;
import blok2.model.calendar.CalendarPeriod;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class DBCalendarPeriodDao extends ADB implements ICalendarPeriodDao {

    private final Logger logger = Logger.getLogger(DBCalendarPeriodDao.class.getSimpleName());

    @Override
    public List<CalendarPeriod> getCalendarPeriodsOfLocation(String locationName) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_calendar_periods"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            List<CalendarPeriod> periods = new ArrayList<>();

            while (rs.next()) {
                periods.add(createCalendarPeriod(rs));
            }

            return periods;
        }
    }

    @Override
    public void addCalendarPeriods(List<CalendarPeriod> periods) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                for (CalendarPeriod calendarPeriod : periods) {
                    addCalendarPeriod(calendarPeriod, conn);
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

    private void addCalendarPeriod(CalendarPeriod calendarPeriod, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_calendar_period"));
        prepareCalendarPeriodPstmt(calendarPeriod, pstmt);
        pstmt.execute();
    }

    @Override
    public void updateCalendarPeriods(List<CalendarPeriod> from, List<CalendarPeriod> to) throws SQLException {
        if (from.size() != to.size()) {
            logger.warning("Update calendar periods: from-list has different sizing as opposed to the to-list");
            return;
        }

        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                for (int i = 0; i < from.size(); i++) {
                    updateCalendarPeriod(from.get(i), to.get(i), conn);
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

    private void updateCalendarPeriod(CalendarPeriod from, CalendarPeriod to, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("update_calendar_period"));
        // set ...
        prepareCalendarPeriodPstmt(to, pstmt);
        // where ...
        prepareWhereClauseOfUpdatePstmt(from, pstmt);
        pstmt.execute();
    }

    @Override
    public void deleteCalendarPeriods(List<CalendarPeriod> periods) throws SQLException {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);

                for (CalendarPeriod calendarPeriod : periods) {
                    deleteCalendarPeriod(calendarPeriod, conn);
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

    private void deleteCalendarPeriod(CalendarPeriod calendarPeriod, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_calendar_period"));
        prepareCalendarPeriodPstmt(calendarPeriod, pstmt);
        pstmt.execute();
    }

    private CalendarPeriod createCalendarPeriod(ResultSet rs) throws SQLException {
        CalendarPeriod calendarPeriod = new CalendarPeriod();

        calendarPeriod.setStartsAt(rs.getString(databaseProperties.getString("calendar_period_starts_at")));
        calendarPeriod.setEndsAt(rs.getString(databaseProperties.getString("calendar_period_ends_at")));
        calendarPeriod.setOpeningTime(rs.getString(databaseProperties.getString("calendar_period_opening_time")));
        calendarPeriod.setClosingTime(rs.getString(databaseProperties.getString("calendar_period_closing_time")));
        calendarPeriod.setReservableFrom(rs.getString(databaseProperties.getString("calendar_period_reservable_from")));

        calendarPeriod.setLocation(DBLocationDao.createLocation(rs));

        return calendarPeriod;
    }

    private void prepareCalendarPeriodPstmt(CalendarPeriod calendarPeriod,
                                            PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, calendarPeriod.getLocation().getName());
        pstmt.setString(2, calendarPeriod.getStartsAt());
        pstmt.setString(3, calendarPeriod.getEndsAt());
        pstmt.setString(4, calendarPeriod.getOpeningTime());
        pstmt.setString(5, calendarPeriod.getClosingTime());
        pstmt.setString(6, calendarPeriod.getReservableFrom());
    }

    private void prepareWhereClauseOfUpdatePstmt(CalendarPeriod calendarPeriod,
                                                 PreparedStatement pstmt) throws SQLException {
        pstmt.setString(7, calendarPeriod.getLocation().getName());
        pstmt.setString(8, calendarPeriod.getStartsAt());
        pstmt.setString(9, calendarPeriod.getEndsAt());
        pstmt.setString(10, calendarPeriod.getOpeningTime());
        pstmt.setString(11, calendarPeriod.getClosingTime());
        pstmt.setString(12, calendarPeriod.getReservableFrom());
    }
}
