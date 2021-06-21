package blok2.daos.db;

import blok2.daos.ITimeslotDAO;
import blok2.helpers.Resources;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import org.springframework.stereotype.Service;
import org.threeten.extra.YearWeek;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static blok2.daos.db.DBLocationDao.createLocation;

@Service
public class DBTimeslotDao extends DAO implements ITimeslotDAO {


    private final Logger logger = Logger.getLogger(DBTimeslotDao.class.getSimpleName());

    @Override
    public List<Timeslot> getTimeslotsOfLocation(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_timeslots_by_location"));
            pstmt.setInt(1, locationId);
            ResultSet rs = pstmt.executeQuery();

            return createTimeslots(rs, conn);
        }
    }

    public Timeslot getTimeslot(int timeslotSeqNr) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_timeslot_by_id"));
            pstmt.setInt(1, timeslotSeqNr);
            ResultSet rs = pstmt.executeQuery();

            if(!rs.next())
                return null;

            return createTimeslot(rs, conn);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw throwables;
        }
    }

    @Override
    public List<Timeslot> addTimeslots(List<Timeslot> timeslots) throws SQLException {
        try(Connection conn = adb.getConnection()) {
            List<Timeslot> list = new ArrayList<>();
            for (Timeslot t : timeslots) {
                Timeslot timeslot = addTimeslot(t, conn);
                list.add(timeslot);
            }
            return list;
        }
    }


    @Override
    public Timeslot addTimeslot(Timeslot timeslot) throws SQLException {
        try(Connection conn = adb.getConnection()) {
           return addTimeslot(timeslot, conn);
        }
    }

    @Override
    public void deleteTimeslot(Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_timeslot"));
            pstmt.setInt(1, timeslot.getTimeslotSequenceNumber());
            pstmt.executeQuery();
        }
    }

    private Timeslot addTimeslot(Timeslot timeslot, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_timeslots"), Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, timeslot.getWeek().getYear());
        pstmt.setInt(2, timeslot.getWeek().getWeek());
        pstmt.setInt(3, timeslot.getDayOfWeek().getValue());
        pstmt.setTime(4, Time.valueOf(timeslot.getOpeningHour()));
        pstmt.setTime(5, Time.valueOf(timeslot.getClosingHour()));
        pstmt.setBoolean(6, timeslot.isReservable());
        pstmt.setTimestamp(7, Timestamp.valueOf(timeslot.getReservableFrom()));
        pstmt.setInt(8, timeslot.getLocationId());
        pstmt.setInt(9, timeslot.getSeatCount());
        pstmt.execute();
        ResultSet gen = pstmt.getGeneratedKeys();
        gen.next();
        timeslot.setTimeslotSequenceNumber(gen.getInt(Resources.databaseProperties.getString("timeslot_sequence_number")));
        return timeslot;
    }

    public static Timeslot getCurrentTimeslot(Location location, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_current_and_or_next_timeslot"));
        pstmt.setInt(1, location.getLocationId());
        ResultSet rs = pstmt.executeQuery();

        if (!rs.next()) {
            return null;
        }

        return createTimeslot(rs, conn);
    }

    public static List<Timeslot> createTimeslots(ResultSet rs, Connection conn) throws SQLException {
        List<Timeslot> ts = new ArrayList<>();
        while (rs.next()) {
            ts.add(createTimeslot(rs, conn));
        }
        return ts;
    }

    public static Timeslot createTimeslot(ResultSet rs, Connection conn) throws SQLException {
        int seqnr = (rs.getInt(Resources.databaseProperties.getString("timeslot_sequence_number")));
        int isodayOfWeek = (rs.getInt(Resources.databaseProperties.getString("timeslot_isoday_of_week")));
        int isoWeek = (rs.getInt(Resources.databaseProperties.getString("timeslot_isoweek")));
        int isoYear = (rs.getInt(Resources.databaseProperties.getString("timeslot_isoyear")));

        LocalDateTime reservableFrom = rs.getTimestamp(Resources.databaseProperties.getString("timeslot_reservable_from")).toLocalDateTime();
        boolean reservable = (rs.getBoolean(Resources.databaseProperties.getString("timeslot_reservable")));
        int count = rs.getInt(Resources.databaseProperties.getString("timeslot_reservation_count"));
        int seatCount = rs.getInt(Resources.databaseProperties.getString("timeslot_seat_count"));
        LocalTime startTime = rs.getTime(Resources.databaseProperties.getString("timeslot_opening_hour")).toLocalTime();
        LocalTime endTime = rs.getTime(Resources.databaseProperties.getString("timeslot_closing_hour")).toLocalTime();
        int locationId = rs.getInt(Resources.databaseProperties.getString("timeslot_location_id"));

        return new Timeslot( seqnr, DayOfWeek.of(isodayOfWeek), YearWeek.of(isoYear, isoWeek), startTime, endTime, reservable, reservableFrom, seatCount, count, locationId);

    }
}
