package blok2.daos.db;

import blok2.daos.ICalendarPeriodDao;
import blok2.helpers.Resources;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
                periods.add(createCalendarPeriod(rs,conn));
            }

            for(CalendarPeriod p : periods) {
                if(p.isReservable())
                    fillTimeslotList(p, conn);
            }

            return periods;
        }
    }

    @Override
    public List<CalendarPeriod> getAllCalendarPeriods() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_all_calendar_periods"));
            ResultSet rs = pstmt.executeQuery();

            List<CalendarPeriod> periods = new ArrayList<>();

            while (rs.next()) {
                periods.add(createCalendarPeriod(rs,conn));
            }

            for(CalendarPeriod p : periods) {
                if(p.isReservable())
                    fillTimeslotList(p, conn);
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


        // Add all relevant timeperiods
        if(calendarPeriod.isReservable()) {
            // One per day (end day inclusive)
            for(LocalDate currDate = calendarPeriod.getStartdateAsDate(); !currDate.isAfter(calendarPeriod.getEndDateAsDate()); currDate=currDate.plusDays(1)) {
                // One per hour (end hour/rest of hour non inclusive)
                int timeslotCount = calendarPeriod.getOpenHoursDuration() / (60*calendarPeriod.getReservableTimeslotSize());
                for(int sequenceNr = 0; sequenceNr < timeslotCount; sequenceNr+=1) {
                    addTimeslotPeriod(sequenceNr, currDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), calendarPeriod, conn);
                }
            }
            fillTimeslotList(calendarPeriod, conn);
        }
    }

    private void addTimeslotPeriod(int seq_id, String date, CalendarPeriod period, Connection conn) throws SQLException {
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

    private void deleteCalendarPeriod(CalendarPeriod calendarPeriod, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_calendar_period"));
        prepareCalendarPeriodPstmt(calendarPeriod, pstmt);
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

        calendarPeriod.setStartsAt(rs.getString(Resources.databaseProperties.getString("calendar_period_starts_at")));
        calendarPeriod.setEndsAt(rs.getString(Resources.databaseProperties.getString("calendar_period_ends_at")));
        calendarPeriod.setOpeningTime(rs.getString(Resources.databaseProperties.getString("calendar_period_opening_time")));
        calendarPeriod.setClosingTime(rs.getString(Resources.databaseProperties.getString("calendar_period_closing_time")));
        calendarPeriod.setReservableFrom(rs.getString(Resources.databaseProperties.getString("calendar_period_reservable_from")));
        calendarPeriod.setReservable(rs.getBoolean(Resources.databaseProperties.getString("calendar_period_reservable")));
        calendarPeriod.setId(rs.getInt(Resources.databaseProperties.getString("calendar_period_id")));
        calendarPeriod.setReservableTimeslotSize(rs.getInt(Resources.databaseProperties.getString("calendar_period_timeslot_length")));
        calendarPeriod.setLocation(DBLocationDao.createLocation(rs,conn));

        return calendarPeriod;
    }

    public static Timeslot createTimeslot(ResultSet rs) throws SQLException {
        Timeslot timeslot = new Timeslot();

        timeslot.setCalendarId(rs.getInt(Resources.databaseProperties.getString("timeslot_calendar_id")));
        timeslot.setTimeslotSeqnr(rs.getInt(Resources.databaseProperties.getString("timeslot_sequence_number")));
        timeslot.setTimeslotDate(rs.getString(Resources.databaseProperties.getString("timeslot_date")));

        return timeslot;
    }

    private void prepareCalendarPeriodPstmt(CalendarPeriod calendarPeriod,
                                            PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, calendarPeriod.getLocation().getName());
        pstmt.setString(2, calendarPeriod.getStartsAt());
        pstmt.setString(3, calendarPeriod.getEndsAt());
        pstmt.setString(4, calendarPeriod.getOpeningTime());
        pstmt.setString(5, calendarPeriod.getClosingTime());
        pstmt.setString(6, calendarPeriod.getReservableFrom());
        pstmt.setBoolean(7, calendarPeriod.isReservable());
        pstmt.setInt(8, calendarPeriod.getReservableTimeslotSize());
    }

    private void prepareWhereClauseOfUpdatePstmt(CalendarPeriod calendarPeriod,
                                                 PreparedStatement pstmt) throws SQLException {
        pstmt.setString(8, calendarPeriod.getLocation().getName());
        pstmt.setString(9, calendarPeriod.getStartsAt());
        pstmt.setString(10, calendarPeriod.getEndsAt());
        pstmt.setString(11, calendarPeriod.getOpeningTime());
        pstmt.setString(12, calendarPeriod.getClosingTime());
        pstmt.setString(13, calendarPeriod.getReservableFrom());
        pstmt.setBoolean(14, calendarPeriod.isReservable());
    }

    private void prepareTimeslotPeriodPstmt(int seq_id, String date, CalendarPeriod period, PreparedStatement pstmt) throws SQLException {
        pstmt.setInt(1, period.getId());
        pstmt.setInt(2, seq_id);
        pstmt.setString(3, date);
    }
}
