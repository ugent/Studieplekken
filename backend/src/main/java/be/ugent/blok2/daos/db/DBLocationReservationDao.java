package be.ugent.blok2.daos.db;

import be.ugent.blok2.daos.ILocationReservationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservations.LocationReservation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Profile("!dummy")
@Service
public class DBLocationReservationDao extends ADB implements ILocationReservationDao {

    @Override
    public List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws NoSuchUserException {
        try (Connection conn = getConnection()) {

            String queryUser = databaseProperties.getString("get_user_by_<?>").replace("<?>", "u.augentID = ?");
            PreparedStatement statementUser = conn.prepareStatement(queryUser);
            statementUser.setString(1, augentID);
            ResultSet resultSetUser = statementUser.executeQuery();

            if (!resultSetUser.next()) {
                throw new NoSuchUserException("No user with id = " + augentID);
            }

            List<LocationReservation> reservations = new ArrayList<>();
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location_reservations_of_user_by_id"));
            st.setString(1, augentID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                try {
                    LocationReservation lockerReservation = createLocationReservation(rs);
                    reservations.add(lockerReservation);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return reservations;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocation(String name) {
        try (Connection conn = getConnection()) {
            List<LocationReservation> reservations = new ArrayList<>();
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location_reservations_of_location"));
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                try {
                    LocationReservation locationReservation = createLocationReservation(rs);
                    reservations.add(locationReservation);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            return reservations;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public LocationReservation getLocationReservation(String augentID, CustomDate date) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("get_location_reservation"));
            st.setString(1, augentID);
            st.setString(2, date.toString());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                try {
                    return createLocationReservation(rs);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteLocationReservation(String augentID, CustomDate date) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("delete_location_reservation"));
            st.setString(1, augentID);
            st.setString(2, date.toString());
            st.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void addLocationReservation(LocationReservation locationReservation) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("insert_location_reservation"));
            st.setString(1, locationReservation.getDate().toString());
            st.setString(2, locationReservation.getLocation().getName());
            st.setString(3, locationReservation.getUser().getAugentID());
            st.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    // TODO: barcode moet augentid worden (zie issue #11)
    public LocationReservation scanStudent(String location, String augentId) {
        try (Connection conn = getConnection()) {
            // find out the CustomDate of today (note: Calendar here is java.util.Calendar,
            // not be.ugent.blok2.helpers.Calendar
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

            pstmt = conn.prepareStatement(databaseProperties.getString("get_location_reservation"));
            pstmt.setString(1, augentId);
            pstmt.setString(2, today.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return createLocationReservation(rs);
            else
                return null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public void setAllStudentsOfLocationToAttended(String location, CustomDate date) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("set_all_location_reservations_attended"));
            st.setString(1, location);
            st.setString(2, date.toString());
            st.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public int countReservedSeatsOfLocationOnDate(String location, CustomDate date) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("count_location_reservations_of_location_for_date"));
            st.setString(1, location);
            st.setString(2, date.toString());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    @Override
    public List<LocationReservation> getAbsentStudents(String locationName, CustomDate date) {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_absent_students"));
            return getAbsentOrPresentStudents(locationName, date, pstmt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public List<LocationReservation> getPresentStudents(String locationName, CustomDate date) {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(databaseProperties.getString("get_present_students"));
            return getAbsentOrPresentStudents(locationName, date, pstmt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
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
    public void setReservationToUnAttended(String augentId, CustomDate date) {
        try (Connection conn = getConnection()) {
            PreparedStatement st = conn.prepareStatement(databaseProperties.getString("set_location_reservation_unattended"));
            st.setString(1, date.toString());
            st.setString(2, augentId);
            st.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static LocationReservation createLocationReservation(ResultSet rs) throws SQLException {
        CustomDate customDate = CustomDate.parseString(rs.getString(databaseProperties.getString("location_reservation_date")));
        // Note: it is important that createUser is called before createLocation.
        //  the reason is that within createLocation, the ResultSet is looped
        //  because it needs all descriptions. But if you would use the looped
        //  ResultSet, the internal record pointer is after the last entry and you
        //  cant go back. So first call createUser(), then createLocation.
        User user = DBAccountDao.createUser(rs);
        Location location = DBLocationDao.createLocation(rs);
        return new LocationReservation(location, user, customDate);
    }
}
