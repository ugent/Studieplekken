package blok2.daos.db;

import blok2.daos.ILocationReservationDao;
import blok2.helpers.Resources;
import blok2.helpers.date.CustomDate;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import blok2.shared.Utility;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Service
public class DBLocationReservationDao extends DAO implements ILocationReservationDao {

    /*
    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocation(String locationName, boolean includePastReservations) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");

            String replacementString = "lr.location_name = ?";
            if (!includePastReservations) {
                replacementString += " and to_date(lr.date, 'YYYY-MM-DD') >= to_date(?, 'YYYY-MM-DD')";
            }
            query = query.replace("<?>", replacementString);

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, locationName);

            if (!includePastReservations) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                pstmt.setString(2, format.format(new Date()));
            }

            return executeQueryForLocationReservations(pstmt, conn);
        }
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocationFrom(String locationName, String start, boolean includePastReservations) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");

            String replacementString = "lr.location_name = ? and to_date(lr.date, 'YYYY-MM-DD') >= to_date(?, 'YYYY-MM-DD')";
            return getLocationReservationsExtractedEqualCode(locationName, start, includePastReservations, conn, query, replacementString);
        }
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocationUntil(String locationName, String end, boolean includePastReservations) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");

            String replacementString = "lr.location_name = ? and to_date(lr.date, 'YYYY-MM-DD') <= to_date(?, 'YYYY-MM-DD')";
            return getLocationReservationsExtractedEqualCode(locationName, end, includePastReservations, conn, query, replacementString);
        }
    }
    */

    private List<LocationReservation> getLocationReservationsExtractedEqualCode(String locationName, String date, boolean includePastReservations, Connection conn, String query, String replacementString) throws SQLException {
        if (!includePastReservations) {
            replacementString += " and to_date(lr.date, 'YYYY-MM-DD') >= to_date(?, 'YYYY-MM-DD')";
        }
        query = query.replace("<?>", replacementString);

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, locationName);
        pstmt.setString(2, Utility.formatDate_YYYY_MM_DD(date));

        if (!includePastReservations) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            pstmt.setString(3, format.format(new Date()));
        }

        return executeQueryForLocationReservations(pstmt, conn);
    }


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
                LocationReservation locationReservation = createLocationReservation(rs,conn);
                reservations.add(locationReservation);
            }

            return reservations;
        }
    }

    private List<LocationReservation> executeQueryForLocationReservations(PreparedStatement pstmt, Connection conn) throws SQLException {
        ResultSet rs = pstmt.executeQuery();

        List<LocationReservation> reservations = new ArrayList<>();
        while (rs.next()) {
            LocationReservation locationReservation = createLocationReservation(rs,conn);
            reservations.add(locationReservation);
        }

        return reservations;
    }

    @Override
    public LocationReservation getLocationReservation(String augentID, Timeslot timeslot) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.user_augentid = ? and lr.timeslot_date = ? and lr.timeslot_seqnr = ? and lr.calendar_id = ?");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, augentID);
            pstmt.setString(2, timeslot.getTimeslotDate());
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
            pstmt.setString(1, timeslot.getTimeslotDate());
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
            pstmt.setString(2, timeslot.getTimeslotDate());
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
        pstmt.setString(2, locationReservation.getCreatedAt());
        pstmt.setString(3, locationReservation.getTimeslot().getTimeslotDate());
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

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                if (amountOfReservations < sizeOfLocation) {
                    System.out.println(amountOfReservations);
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
    public LocationReservation scanStudent(String location, String augentId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            Calendar c = Calendar.getInstance();
            CustomDate today = new CustomDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE));

            // set user attended on location reservation
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("set_location_reservation_attended"));
            pstmt.setString(1, today.toString());
            pstmt.setString(2, augentId);
            int n = pstmt.executeUpdate();

            // report error if no location could be found
            if (n != 1)
                return null;

            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.user_augentid = ? and lr.date = ?");
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, augentId);
            pstmt.setString(2, today.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return createLocationReservation(rs, conn);
            else
                return null;
        }
    }

    @Override
    public void setAllStudentsOfLocationToAttended(String location, CustomDate date) throws SQLException {
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
    public List<LocationReservation> getAbsentStudents(String locationName, CustomDate date) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.location_name = ? and lr.date = ? and (lr.attended = false or lr.attended is null)");
            PreparedStatement pstmt = conn.prepareStatement(query);
            return getAbsentOrPresentStudents(locationName, date, pstmt, conn);
        }
    }

    @Override
    public List<LocationReservation> getPresentStudents(String locationName, CustomDate date) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            String query = Resources.databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.location_name = ? and lr.date = ? and lr.attended = true");
            PreparedStatement pstmt = conn.prepareStatement(query);
            return getAbsentOrPresentStudents(locationName, date, pstmt, conn);
        }
    }

    private List<LocationReservation> getAbsentOrPresentStudents(String locationName, CustomDate date
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
    public void setReservationToUnAttended(String augentId, CustomDate date) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("set_location_reservation_unattended"));
            pstmt.setString(1, date.toString());
            pstmt.setString(2, augentId);
            pstmt.execute();
        }
    }

    // Seperated out for use in transaction
    public long getAmountOfReservationsOfTimeslot(Timeslot timeslot, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_location_reservations_of_location_for_timeslot"));
        pstmt.setInt(1, timeslot.getCalendarId());
        pstmt.setString(2, timeslot.getTimeslotDate());
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

    public static LocationReservation createLocationReservation(ResultSet rs,Connection conn) throws SQLException {

        Boolean attended = rs.getBoolean(Resources.databaseProperties.getString("location_reservation_attended"));
        if (rs.wasNull()) {
            attended = null;
        }

        // Note: it is important that createUser is called before createLocation.
        //  the reason is that within createLocation, the ResultSet is looped
        //  because it needs all descriptions. But if you would use the looped
        //  ResultSet, the internal record pointer is after the last entry and you
        //  cant go back. So first call createUser(), then createLocation.
        User user = DBAccountDao.createUser(rs);
        Timeslot timeslot = DBCalendarPeriodDao.createTimeslot(rs);
        String createdAt = rs.getString(Resources.databaseProperties.getString("location_reservation_created_at"));

        return new LocationReservation(user, createdAt, timeslot, attended);
    }


}
