package blok2.daos.db;

import blok2.daos.ILocationReservationDao;
import blok2.helpers.date.CustomDate;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import blok2.shared.Utility;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DBLocationReservationDao extends ADB implements ILocationReservationDao {

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocation(String locationName, boolean includePastReservations) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_location_reservations_where_<?>");

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

            return executeQueryForLocationReservations(pstmt);
        }
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocationFrom(String locationName, String start, boolean includePastReservations) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_location_reservations_where_<?>");

            String replacementString = "lr.location_name = ? and to_date(lr.date, 'YYYY-MM-DD') >= to_date(?, 'YYYY-MM-DD')";
            return getLocationReservationsExtractedEqualCode(locationName, start, includePastReservations, conn, query, replacementString);
        }
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocationUntil(String locationName, String end, boolean includePastReservations) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_location_reservations_where_<?>");

            String replacementString = "lr.location_name = ? and to_date(lr.date, 'YYYY-MM-DD') <= to_date(?, 'YYYY-MM-DD')";
            return getLocationReservationsExtractedEqualCode(locationName, end, includePastReservations, conn, query, replacementString);
        }
    }

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

        return executeQueryForLocationReservations(pstmt);
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocationFromAndUntil(String locationName, String start, String end, boolean includePastReservations) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_location_reservations_where_<?>");

            String replacementString = "lr.location_name = ? and to_date(lr.date, 'YYYY-MM-DD') >= to_date(?, 'YYYY-MM-DD') and to_date(lr.date, 'YYYY-MM-DD') <= to_date(?, 'YYYY-MM-DD')";
            if (!includePastReservations) {
                replacementString += " and to_date(lr.date, 'YYYY-MM-DD') >= to_date(?, 'YYYY-MM-DD')";
            }
            query = query.replace("<?>", replacementString);

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, locationName);
            pstmt.setString(2, Utility.formatDate_YYYY_MM_DD(start));
            pstmt.setString(3, Utility.formatDate_YYYY_MM_DD(end));

            if (!includePastReservations) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                pstmt.setString(4, format.format(new Date()));
            }

            return executeQueryForLocationReservations(pstmt);
        }
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws SQLException {
        String query = databaseProperties.getString("get_location_reservations_where_<?>");
        query = query.replace("<?>", "u.augentid = ?");
        return getAllLocationsFromQueryWithOneParameter(augentID, query);
    }

    private List<LocationReservation> getAllLocationsFromQueryWithOneParameter(String parameterOne, String query)
            throws SQLException {
        try (Connection conn = getConnection()) {
            List<LocationReservation> reservations = new ArrayList<>();

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, parameterOne);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocationReservation locationReservation = createLocationReservation(rs);
                reservations.add(locationReservation);
            }

            return reservations;
        }
    }

    private List<LocationReservation> executeQueryForLocationReservations(PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();

        List<LocationReservation> reservations = new ArrayList<>();
        while (rs.next()) {
            LocationReservation locationReservation = createLocationReservation(rs);
            reservations.add(locationReservation);
        }

        return reservations;
    }

    @Override
    public LocationReservation getLocationReservation(String augentID, CustomDate date) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.user_augentid = ? and lr.date = ?");

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, augentID);
            pstmt.setString(2, date.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createLocationReservation(rs);
            } else {
                return null;
            }
        }
    }

    @Override
    public void deleteLocationReservation(String augentID, CustomDate date) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("delete_location_reservation"));
            pstmt.setString(1, augentID);
            pstmt.setString(2, date.toString());
            pstmt.execute();
        }
    }

    @Override
    public void addLocationReservation(LocationReservation locationReservation) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("insert_location_reservation"));
            pstmt.setString(1, locationReservation.getDate().toString());
            pstmt.setString(2, locationReservation.getLocation().getName());
            pstmt.setString(3, locationReservation.getUser().getAugentID());
            pstmt.execute();
        }
    }

    @Override
    public LocationReservation scanStudent(String location, String augentId) throws SQLException {
        try (Connection conn = getConnection()) {
            Calendar c = Calendar.getInstance();
            CustomDate today = new CustomDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE));

            // set user attended on location reservation
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("set_location_reservation_attended"));
            pstmt.setString(1, today.toString());
            pstmt.setString(2, augentId);
            int n = pstmt.executeUpdate();

            // report error if no location could be found
            if (n != 1)
                return null;

            String query = databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.user_augentid = ? and lr.date = ?");
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, augentId);
            pstmt.setString(2, today.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return createLocationReservation(rs);
            else
                return null;
        }
    }

    @Override
    public void setAllStudentsOfLocationToAttended(String location, CustomDate date) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("set_all_location_reservations_attended"));
            pstmt.setString(1, location);
            pstmt.setString(2, date.toString());
            pstmt.execute();
        }
    }

    @Override
    public int countReservedSeatsOfLocationOnDate(String location, CustomDate date) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("count_location_reservations_of_location_for_date"));
            pstmt.setString(1, location);
            pstmt.setString(2, date.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    @Override
    public List<LocationReservation> getAbsentStudents(String locationName, CustomDate date) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.location_name = ? and lr.date = ? and (lr.attended = false or lr.attended is null)");
            PreparedStatement pstmt = conn.prepareStatement(query);
            return getAbsentOrPresentStudents(locationName, date, pstmt);
        }
    }

    @Override
    public List<LocationReservation> getPresentStudents(String locationName, CustomDate date) throws SQLException {
        try (Connection conn = getConnection()) {
            String query = databaseProperties.getString("get_location_reservations_where_<?>");
            query = query.replace("<?>", "lr.location_name = ? and lr.date = ? and lr.attended = true");
            PreparedStatement pstmt = conn.prepareStatement(query);
            return getAbsentOrPresentStudents(locationName, date, pstmt);
        }
    }

    private List<LocationReservation> getAbsentOrPresentStudents(String locationName, CustomDate date
            , PreparedStatement pstmt) throws SQLException {
        List<LocationReservation> reservations = new ArrayList<>();

        pstmt.setString(1, locationName);
        pstmt.setString(2, date.toString());
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            LocationReservation locationReservation = createLocationReservation(rs);
            reservations.add(locationReservation);
        }

        return reservations;
    }

    @Override
    public void setReservationToUnAttended(String augentId, CustomDate date) throws SQLException {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("set_location_reservation_unattended"));
            pstmt.setString(1, date.toString());
            pstmt.setString(2, augentId);
            pstmt.execute();
        }
    }

    public static LocationReservation createLocationReservation(ResultSet rs) throws SQLException {
        CustomDate customDate = CustomDate.parseString(rs.getString(databaseProperties.getString("location_reservation_date")));

        Boolean attended = rs.getBoolean(databaseProperties.getString("location_reservation_attended"));
        if (rs.wasNull()) {
            attended = null;
        }

        // Note: it is important that createUser is called before createLocation.
        //  the reason is that within createLocation, the ResultSet is looped
        //  because it needs all descriptions. But if you would use the looped
        //  ResultSet, the internal record pointer is after the last entry and you
        //  cant go back. So first call createUser(), then createLocation.
        User user = DBAccountDao.createUser(rs);
        Location location = DBLocationDao.createLocation(rs);
        return new LocationReservation(location, user, customDate, attended);
    }
}
