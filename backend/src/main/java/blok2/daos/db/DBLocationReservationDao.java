package blok2.daos.db;

import blok2.daos.ILocationReservationDao;
import blok2.helpers.Pair;
import blok2.helpers.Resources;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DBLocationReservationDao extends DAO implements ILocationReservationDao {

    @Override
    public List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws SQLException {
        String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
        query = query.replace("<?>", "u.augentid = ?");
        return getAllLocationsFromQueryWithOneParameter(augentID, query);
    }

    private List<LocationReservation> getAllLocationsFromQueryWithOneParameter(String parameterOne, String query)
            throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<LocationReservation> reservations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, parameterOne);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocationReservation locationReservation = createLocationReservation(rs, conn);
                reservations.add(locationReservation);
            }

            return reservations;
        }
    }

    @Override
    public List<Pair<LocationReservation, Location>> getAllLocationReservationsWithLocationOfUser(String userId)
            throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Pair<LocationReservation, Location>> reservations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(Resources
                    .databaseProperties.getString("get_location_reservations_with_location_by_user"));
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocationReservation lr = createLocationReservation(rs, conn);
                Location l = DBLocationDao.createLocation(rs, conn);
                reservations.add(new Pair<>(lr, l));
            }

            return reservations;
        }
    }

    @Override
    public LocationReservation getLocationReservation(String augentID, Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.user_augentid = ? and lr.timeslot_date = ? and lr.timeslot_seqnr = ? and lr.calendar_id = ?");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, augentID);
            pstmt.setDate(2, java.sql.Date.valueOf(timeslot.getTimeslotDate()));
            pstmt.setInt(3, timeslot.getTimeslotSeqnr());
            pstmt.setInt(4, timeslot.getCalendarId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createLocationReservation(rs, conn);
            } else {
                return null;
            }
        }
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfTimeslot(Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.timeslot_date = ? and lr.timeslot_seqnr = ? and lr.calendar_id = ?");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setDate(1, java.sql.Date.valueOf(timeslot.getTimeslotDate()));
            pstmt.setInt(2, timeslot.getTimeslotSeqnr());
            pstmt.setInt(3, timeslot.getCalendarId());
            ResultSet rs = pstmt.executeQuery();

            List<LocationReservation> rlist = new ArrayList<>();
            while (rs.next()) {
                rlist.add(createLocationReservation(rs, conn));
            }

            return Collections.unmodifiableList(rlist);
        }
    }

    @Override
    public void deleteLocationReservation(String augentID, Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_location_reservation"));
            pstmt.setString(1, augentID);
            pstmt.setDate(2, java.sql.Date.valueOf(timeslot.getTimeslotDate()));
            pstmt.setInt(3, timeslot.getTimeslotSeqnr());
            pstmt.setInt(4, timeslot.getCalendarId());
            pstmt.execute();
        }
    }

    @Override
    public void addLocationReservation(LocationReservation locationReservation) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            addLocationReservation(locationReservation, conn);
        }
    }

    private void addLocationReservation(LocationReservation locationReservation, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_location_reservation"));
        pstmt.setString(1, locationReservation.getUser().getAugentID());
        pstmt.setTimestamp(2, Timestamp.valueOf(locationReservation.getCreatedAt()));
        pstmt.setDate(3, java.sql.Date.valueOf(locationReservation.getTimeslot().getTimeslotDate()));
        pstmt.setInt(4, locationReservation.getTimeslot().getTimeslotSeqnr());
        pstmt.setInt(5, locationReservation.getTimeslot().getCalendarId());
        pstmt.execute();

    }

    @Override
    public boolean addLocationReservationIfStillRoomAtomically(LocationReservation reservation) throws SQLException {
        // Open up transaction
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                // Take a lock on the database.
                conn.setAutoCommit(false);
                conn.prepareStatement(Resources.databaseProperties.getString("lock_location_reservation")).execute();

                // Fetch data we need.
                long amountOfReservations = getAmountOfReservationsOfTimeslot(reservation.getTimeslot(), conn);

                long sizeOfLocation = getLocationSizeOfTimeslot(reservation.getTimeslot(), conn);

                if (amountOfReservations < sizeOfLocation) {
                    // All is well. Add & then release the lock

                    addLocationReservation(reservation, conn);
                    conn.commit();
                    return true;
                }

                return false;

            } catch (SQLException e) {
                conn.rollback();
                // Error codes that start with "23" are constraint violations.
                // This means that the entry was probably not unique.
                if (e.getSQLState().startsWith("23")) {
                    return false;
                } else {
                    // This is a real db error. Rethrowing it.
                    throw e;
                }
            }
        }
    }

    @Override
    public int amountOfReservationsRightNow(String location) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_reservations_now"));
            pstmt.setString(1, location);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            if(rs.wasNull())
                return 0;
            if(rs.getInt(2) == 0) {
                return -1;
            }

            return rs.getInt(1);
        }
    }

    @Override
    public LocationReservation scanStudent(String location, String augentId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            // set user attended on location reservation
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("set_location_reservation_attended"));
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setString(2, augentId);
            int n = pstmt.executeUpdate();

            // report error if no location could be found
            if (n != 1)
                return null;

            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.user_augentid = ? and lr.date = ?");
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, augentId);
            pstmt.setString(2, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return createLocationReservation(rs, conn);
            else
                return null;
        }
    }

    @Override
    public void setAllStudentsOfLocationToAttended(String location, LocalDate date) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("set_all_location_reservations_attended"));
            pstmt.setString(1, location);
            pstmt.setString(2, date.toString());
            pstmt.execute();
        }
    }

    @Override
    public long countReservedSeatsOfTimeslot(Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getAmountOfReservationsOfTimeslot(timeslot, conn);
        }
    }

    @Override
    public List<LocationReservation> getAbsentStudents(String locationName, LocalDate date) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.location_name = ? and lr.date = ? and (lr.attended = false or lr.attended is null)");
            PreparedStatement pstmt = conn.prepareStatement(query);
            return getAbsentOrPresentStudents(locationName, date, pstmt, conn);
        }
    }

    @Override
    public List<LocationReservation> getPresentStudents(String locationName, LocalDate date) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.location_name = ? and lr.date = ? and lr.attended = true");
            PreparedStatement pstmt = conn.prepareStatement(query);
            return getAbsentOrPresentStudents(locationName, date, pstmt, conn);
        }
    }

    public static int amountOfTimeslotReservations(Timeslot timeslot, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_location_reservations_of_location_for_timeslot"));
        pstmt.setInt(1, timeslot.getCalendarId());
        pstmt.setDate(2, Date.valueOf(timeslot.getTimeslotDate()));
        pstmt.setInt(3, timeslot.getTimeslotSeqnr());
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    private List<LocationReservation> getAbsentOrPresentStudents(String locationName, LocalDate date
            , PreparedStatement pstmt, Connection conn) throws SQLException {
        List<LocationReservation> reservations = new ArrayList<>();

        pstmt.setString(1, locationName);
        pstmt.setString(2, date.toString());
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            LocationReservation locationReservation = createLocationReservation(rs, conn);
            reservations.add(locationReservation);
        }

        return reservations;
    }

    @Override
    public void setReservationAttendance(String augentId, Timeslot timeslot, boolean attendance) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("set_location_reservation_attendance"));
            pstmt.setBoolean(1, attendance);
            pstmt.setInt(2, timeslot.getCalendarId());
            pstmt.setDate(3, Date.valueOf(timeslot.getTimeslotDate()));
            pstmt.setInt(4, timeslot.getTimeslotSeqnr());
            pstmt.setString(5, augentId);
            pstmt.execute();
        }
    }

    // Seperated out for use in transaction
    public long getAmountOfReservationsOfTimeslot(Timeslot timeslot, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_location_reservations_of_location_for_timeslot"));
        pstmt.setInt(1, timeslot.getCalendarId());
        pstmt.setDate(2, java.sql.Date.valueOf(timeslot.getTimeslotDate()));
        pstmt.setInt(3, timeslot.getTimeslotSeqnr());
        ResultSet set = pstmt.executeQuery();

        if(set.next()) {
            return set.getLong(1);
        }

        return -1;
    }

    private long getLocationSizeOfTimeslot(Timeslot timeslot, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_size_of_timeslot_location"));
        pstmt.setInt(1, timeslot.getCalendarId());
        ResultSet set = pstmt.executeQuery();

        if(set.next()) {
            return set.getLong(1);
        }

        return -1;

    }

    public static LocationReservation createLocationReservation(ResultSet rs, Connection conn) throws SQLException {
        Boolean attended = rs.getBoolean(Resources.databaseProperties.getString("location_reservation_attended"));
        if (rs.wasNull()) {
            attended = null;
        }

        User user = DBAccountDao.createUser(rs, conn);
        Timeslot timeslot = DBCalendarPeriodDao.createTimeslot(rs, conn);
        LocalDateTime createdAt = rs.getTimestamp(Resources.databaseProperties.getString("location_reservation_created_at")).toLocalDateTime();

        return new LocationReservation(user, createdAt, timeslot, attended);
    }

}
