package blok2.daos.db;

import blok2.daos.ICalendarPeriodDao;
import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.helpers.Resources;
import blok2.model.calendar.CalendarPeriod;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class DBCalendarPeriodDao extends DAO implements ICalendarPeriodDao {


    private final Logger logger = Logger.getLogger(DBCalendarPeriodDao.class.getSimpleName());

    @Override
    public List<CalendarPeriod> getCalendarPeriodsOfLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_calendar_periods"));
            pstmt.setString(1, locationName);
            ResultSet rs = pstmt.executeQuery();

            List<CalendarPeriod> periods = new ArrayList<>();

            while (rs.next()) {
                periods.add(createCalendarPeriod(rs, conn));
            }

            return periods;
        }
    }

    @Override
    public void addCalendarPeriods(List<CalendarPeriod> periods) throws SQLException {
        try (Connection conn = adb.getConnection()) {
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
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_calendar_period"));
        prepareCalendarPeriodPstmt(calendarPeriod, pstmt);
        pstmt.execute();
    }

    @Override
    public void updateCalendarPeriods(List<CalendarPeriod> from, List<CalendarPeriod> to) throws SQLException {
        if (from.size() != to.size()) {
            logger.warning("Update calendar periods: from-list has different sizing as opposed to the to-list");
            return;
        }

        try (Connection conn = adb.getConnection()) {
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
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_calendar_period"));
        // set ...
        prepareCalendarPeriodPstmt(to, pstmt);
        // where ...
        prepareWhereClauseOfUpdatePstmt(from, pstmt);
        pstmt.execute();
    }

    @Override
    public void deleteCalendarPeriods(List<CalendarPeriod> periods) throws SQLException {
        try (Connection conn = adb.getConnection()) {
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

    @Override
    public Pair<LocationStatus, String> getStatus(String locationName) throws SQLException {
        List<CalendarPeriod> periods = getCalendarPeriodsOfLocation(locationName);

        List<Pair<LocalDateTime, LocalDateTime>> beginAndEndDates = periods.stream()
                .map(CalendarPeriod::getBeginAndEndDate)
                .sorted(Comparator.comparing(Pair::getFirst))
                .collect(Collectors.toList());

        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Pair<LocalDateTime, LocalDateTime> pair : beginAndEndDates) {
            if (pair.getFirst().isAfter(LocalDateTime.now())) {
                return new Pair<>(LocationStatus.CLOSED_UPCOMING, pair.getFirst().format(outputFormat));
            } else {
                if (pair.getSecond().isAfter(LocalDateTime.now())) {
                    if (pair.getFirst().toLocalTime().isBefore(LocalTime.now()) && pair.getSecond().toLocalTime().isAfter(LocalTime.now())) {
                        return new Pair<>(LocationStatus.OPEN, pair.getSecond().format(outputFormat));
                    } else {
                        return new Pair<>(LocationStatus.CLOSED_ACTIVE, pair.getFirst().format(outputFormat));
                    }
                }
            }
        }

        // If none of the calendarperiods are upcoming, the location is closed indefinitely
        return new Pair<>(LocationStatus.CLOSED, "");
    }

    private void deleteCalendarPeriod(CalendarPeriod calendarPeriod, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_calendar_period"));
        prepareCalendarPeriodPstmt(calendarPeriod, pstmt);
        pstmt.execute();
    }

    private CalendarPeriod createCalendarPeriod(ResultSet rs, Connection conn) throws SQLException {
        CalendarPeriod calendarPeriod = new CalendarPeriod();

        calendarPeriod.setStartsAt(rs.getString(Resources.databaseProperties.getString("calendar_period_starts_at")));
        calendarPeriod.setEndsAt(rs.getString(Resources.databaseProperties.getString("calendar_period_ends_at")));
        calendarPeriod.setOpeningTime(rs.getString(Resources.databaseProperties.getString("calendar_period_opening_time")));
        calendarPeriod.setClosingTime(rs.getString(Resources.databaseProperties.getString("calendar_period_closing_time")));
        calendarPeriod.setReservableFrom(rs.getString(Resources.databaseProperties.getString("calendar_period_reservable_from")));

        calendarPeriod.setLocation(DBLocationDao.createLocation(rs, conn));

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
