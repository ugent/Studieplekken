package blok2.daos.db;

import blok2.daos.ICalendarPeriodDao;
import blok2.helpers.Resources;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class DBCalendarPeriodDao extends DAO implements ICalendarPeriodDao {

    private final Logger logger = Logger.getLogger(DBCalendarPeriodDao.class.getSimpleName());

    @Override
    public List<CalendarPeriod> getCalendarPeriodsOfLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_calendar_periods"));
            pstmt.setString(1, locationName);
            return getCalendarPeriodsFromPstmt(pstmt, conn);
        }
    }

    @Override
    public List<CalendarPeriod> getAllCalendarPeriods() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_all_calendar_periods"));
            return getCalendarPeriodsFromPstmt(pstmt, conn);
        }
    }

    public List<CalendarPeriod> getCalendarPeriodsInWeek(LocalDate firstDayOfWeek) throws SQLException {
        LocalDate lastDayOfWeek = firstDayOfWeek.plusWeeks(1);
        return getCalendarPeriodsInPeriod(firstDayOfWeek, lastDayOfWeek);
    }

    public List<CalendarPeriod> getCalendarPeriodsInPeriod(LocalDate start, LocalDate end) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(Resources.databaseProperties.getString("get_calendar_periods_in_period"));
            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            ResultSet rs = stmt.executeQuery();

            List<CalendarPeriod> periods = new ArrayList<>();

            while (rs.next()) {
                periods.add(createCalendarPeriod(rs,conn));
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
        String[] generatedColumns = { Resources.databaseProperties.getString("calendar_period_id") };
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_calendar_period"), generatedColumns);
        prepareCalendarPeriodPstmt(calendarPeriod, pstmt);
        pstmt.execute();

        ResultSet rs = pstmt.getGeneratedKeys();
        rs.next();
        calendarPeriod.setId(rs.getInt(1));

        // Add all relevant time periods
        if(calendarPeriod.isReservable()) {
            // One per day (end day inclusive)
            for(LocalDate currDate = calendarPeriod.getStartsAt(); !currDate.isAfter(calendarPeriod.getEndsAt()); currDate=currDate.plusDays(1)) {
                // One per hour (end hour/rest of hour non inclusive)
                int timeslotCount = calendarPeriod.getOpenHoursDuration() / (60*calendarPeriod.getReservableTimeslotSize());
                for(int sequenceNr = 0; sequenceNr < timeslotCount; sequenceNr+=1) {
                    addTimeslotPeriod(sequenceNr, currDate, calendarPeriod, conn);
                }
            }
            fillTimeslotList(calendarPeriod, conn);
        }
    }

    private void addTimeslotPeriod(int seq_id, LocalDate date, CalendarPeriod period, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_reservation_timeslots"));
        prepareTimeslotPeriodPstmt(seq_id, date, period, pstmt);
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
    public CalendarPeriod getById(int calendarId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                PreparedStatement statement = conn.prepareStatement(Resources.databaseProperties.getString("get_calendar_period_by_id"));
                statement.setInt(1,calendarId);
                ResultSet set = statement.executeQuery();
                set.next();
                return createCalendarPeriod(set, conn);
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    private void deleteCalendarPeriod(CalendarPeriod calendarPeriod, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_calendar_period"));
        prepareCommonPartOfCalendarPeriodPstmt(calendarPeriod, pstmt);
        pstmt.setBoolean(6, calendarPeriod.isReservable());
        pstmt.setInt(7, calendarPeriod.getReservableTimeslotSize());
        pstmt.execute();
    }

    private void fillTimeslotList(CalendarPeriod calendarPeriod, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_reservation_timeslots"));
        pstmt.setInt(1, calendarPeriod.getId());
        ResultSet rs = pstmt.executeQuery();

        List<Timeslot> timeslotList = new ArrayList<>();

        while(rs.next()) {
            timeslotList.add(createTimeslot(rs));
        }

        calendarPeriod.setTimeslots(Collections.unmodifiableList(timeslotList));
    }

    private CalendarPeriod createCalendarPeriod(ResultSet rs, Connection conn) throws SQLException {
        CalendarPeriod calendarPeriod = new CalendarPeriod();

        calendarPeriod.setStartsAt(rs.getDate(Resources.databaseProperties.getString("calendar_period_starts_at")).toLocalDate());
        calendarPeriod.setEndsAt(rs.getDate(Resources.databaseProperties.getString("calendar_period_ends_at")).toLocalDate());
        calendarPeriod.setOpeningTime(rs.getTime(Resources.databaseProperties.getString("calendar_period_opening_time")).toLocalTime());
        calendarPeriod.setClosingTime(rs.getTime(Resources.databaseProperties.getString("calendar_period_closing_time")).toLocalTime());
        calendarPeriod.setReservableFrom(rs.getTimestamp(Resources.databaseProperties.getString("calendar_period_reservable_from")).toLocalDateTime());
        calendarPeriod.setReservable(rs.getBoolean(Resources.databaseProperties.getString("calendar_period_reservable")));
        calendarPeriod.setId(rs.getInt(Resources.databaseProperties.getString("calendar_period_id")));
        calendarPeriod.setReservableTimeslotSize(rs.getInt(Resources.databaseProperties.getString("calendar_period_timeslot_length")));
        calendarPeriod.setLocation(DBLocationDao.createLocation(rs,conn));
        calendarPeriod.setLockedFrom(rs.getTimestamp(Resources.databaseProperties.getString("calendar_period_locked_from")).toLocalDateTime());

        return calendarPeriod;
    }

    public static Timeslot createTimeslot(ResultSet rs) throws SQLException {

        Integer calendarId = (rs.getInt(Resources.databaseProperties.getString("timeslot_calendar_id")));
        Integer seqnr = (rs.getInt(Resources.databaseProperties.getString("timeslot_sequence_number")));
        LocalDate date = (rs.getDate(Resources.databaseProperties.getString("timeslot_date")).toLocalDate());

        return new Timeslot(calendarId, seqnr, date);
    }

    private void prepareCommonPartOfCalendarPeriodPstmt(CalendarPeriod calendarPeriod,
                                                        PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, calendarPeriod.getLocation().getName());
        pstmt.setDate(2, Date.valueOf(calendarPeriod.getStartsAt()));
        pstmt.setDate(3, Date.valueOf(calendarPeriod.getEndsAt()));
        pstmt.setTime(4, Time.valueOf(calendarPeriod.getOpeningTime()));
        pstmt.setTime(5, Time.valueOf(calendarPeriod.getClosingTime()));
    }

    private void prepareCalendarPeriodPstmt(CalendarPeriod calendarPeriod,
                                            PreparedStatement pstmt) throws SQLException {
        prepareCommonPartOfCalendarPeriodPstmt(calendarPeriod, pstmt);
        if(calendarPeriod.getReservableFrom() != null) {
            pstmt.setTimestamp(6, Timestamp.valueOf(calendarPeriod.getReservableFrom()));
        } else {
            pstmt.setNull(6, Types.TIMESTAMP);
        }
        pstmt.setBoolean(7, calendarPeriod.isReservable());
        pstmt.setInt(8, calendarPeriod.getReservableTimeslotSize());
        pstmt.setTimestamp(9, Timestamp.valueOf(calendarPeriod.getLockedFrom()));
    }

    private void prepareWhereClauseOfUpdatePstmt(CalendarPeriod calendarPeriod,
                                                 PreparedStatement pstmt) throws SQLException {
        pstmt.setString(10, calendarPeriod.getLocation().getName());
        pstmt.setDate(11, Date.valueOf(calendarPeriod.getStartsAt()));
        pstmt.setDate(12, Date.valueOf(calendarPeriod.getEndsAt()));
        pstmt.setTime(13, Time.valueOf(calendarPeriod.getOpeningTime()));
        pstmt.setTime(14, Time.valueOf(calendarPeriod.getClosingTime()));
        pstmt.setBoolean(15, calendarPeriod.isReservable());
        pstmt.setInt(16, calendarPeriod.getReservableTimeslotSize());
    }

    private void prepareTimeslotPeriodPstmt(int seq_id, LocalDate date, CalendarPeriod period, PreparedStatement pstmt) throws SQLException {
        pstmt.setInt(1, period.getId());
        pstmt.setInt(2, seq_id);
        pstmt.setDate(3, Date.valueOf(date));
    }

    private List<CalendarPeriod> getCalendarPeriodsFromPstmt(PreparedStatement pstmt, Connection conn) throws SQLException {
        ResultSet rs = pstmt.executeQuery();

        List<CalendarPeriod> periods = new ArrayList<>();

        while (rs.next()) {
            periods.add(createCalendarPeriod(rs, conn));
        }

        for (CalendarPeriod p : periods) {
            if(p.isReservable())
                fillTimeslotList(p, conn);
        }

        return periods;
    }
}
