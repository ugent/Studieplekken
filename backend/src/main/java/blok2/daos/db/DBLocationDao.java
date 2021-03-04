package blok2.daos.db;

import blok2.daos.IAccountDao;
import blok2.daos.ILocationDao;
import blok2.daos.IScannerLocationDao;
import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.helpers.Resources;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static blok2.daos.db.DBAccountDao.createUser;

@Service
public class DBLocationDao extends DAO implements ILocationDao {

    IAccountDao accountDao;
    IScannerLocationDao scannerLocationDao;

    public DBLocationDao(IAccountDao accountDao, IScannerLocationDao scannerLocationDao) {
        this.accountDao = accountDao;
        this.scannerLocationDao = scannerLocationDao;
    }

    @Override
    public List<Location> getAllLocations() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Location> locations = new ArrayList<>();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(Resources.databaseProperties.getString("all_locations"));

            while (rs.next()) {
                Location location = createLocation(rs, conn);
                locations.add(location);
            }

            return locations;
        }
    }

    @Override
    public List<Pair<String, LocalDateTime>> getAllLocationNextReservableFroms() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Pair<String, LocalDateTime>> nextReservableFroms = new ArrayList<>();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(Resources.databaseProperties.getString("all_locations_next_reservable_froms"));

            while (rs.next()) {
                String locationName = rs.getString(Resources.databaseProperties.getString("location_name"));
                LocalDateTime nextReservableFrom = rs.getTimestamp(
                        Resources.databaseProperties.getString("calendar_period_reservable_from")).toLocalDateTime();
                nextReservableFroms.add(new Pair<>(locationName, nextReservableFrom));
            }

            return nextReservableFroms;
        }
    }

    @Override
    public List<Location> getAllUnapprovedLocations() throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Location> locations = new ArrayList<>();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(Resources.databaseProperties.getString("all_unapproved_locations"));

            while (rs.next()) {
                Location location = createLocation(rs, conn);
                locations.add(location);
            }

            return locations;
        }
    }

    @Override
    public Location addLocation(Location location) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return addLocationAsTransaction(location, conn);
        }
    }

    private Location addLocation(Location location, Connection conn) throws SQLException {
        // insert location into the database
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_location"));
        prepareUpdateOrInsertLocationStatement(location, pstmt);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            int locationId = rs.getInt(1);
            location.setLocationId(locationId);

            insertTags(locationId, location.getAssignedTags(), conn);

            // insert the lockers corresponding to the location into the database
            for (int i = 0; i < location.getNumberOfLockers(); i++) {
                insertLocker(locationId, i, conn);
            }
        }

        return null;
    }

    private Location addLocationAsTransaction(Location location, Connection conn) throws SQLException {
        try {
            conn.setAutoCommit(false);
            Location l = addLocation(location, conn);
            conn.commit();
            return l;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public Location getLocationByName(String name) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getLocationByName(name, conn);
        }
    }

    public static Location getLocationByName(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_location_by_name"));
        pstmt.setString(1, locationName);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return createLocation(rs, conn);
        }
        return null;
    }

    @Override
    public Location getLocationById(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getLocationById(locationId, conn);
        }
    }

    public static Location getLocationById(int locationId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_location_by_id"));
        pstmt.setInt(1, locationId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return createLocation(rs, conn);
        }
        return null;
    }

    @Override
    public void updateLocation(int locationId, Location location) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                Location oldLocation = getLocationById(locationId, conn);

                if (oldLocation == null)
                    return;

                // Update location
                PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_location"));
                // set ...
                prepareUpdateOrInsertLocationStatement(location, pstmt);
                // where ...
                pstmt.setInt(10, locationId);
                pstmt.execute();

                // Update lockers if necessary
                if (oldLocation.getNumberOfLockers() != location.getNumberOfLockers()) {
                    updateNumberOfLockers(locationId, oldLocation.getNumberOfLockers()
                            , location.getNumberOfLockers(), conn);
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

    private void updateNumberOfLockers(int locationId, int from, int to, Connection conn) throws SQLException {
        if (from > to) {
            decreaseNumberOfLockers(locationId, from, to, conn);
        } else {
            increaseNumberOfLockers(locationId, from, to, conn);
        }
    }

    private void increaseNumberOfLockers(int locationId, int from, int to, Connection conn) throws SQLException {
        for (int i = from; i < to; i++) {
            insertLocker(locationId, i, conn);
        }
    }

    private void decreaseNumberOfLockers(int locationId, int from, int to, Connection conn) throws SQLException {
        for (int i = from - 1; i >= to; i--) {
            deleteLocker(locationId, i, conn);
        }
    }

    @Override
    public void deleteLocation(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_location"));
            pstmt.setInt(1, locationId);
            pstmt.execute();
        }
    }

    @Override
    public void addVolunteer(int locationId, String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("add_volunteer"));
            pstmt.setString(1, userId);
            pstmt.setInt(2, locationId);
            pstmt.execute();
        }
    }
    @Override
    public void deleteVolunteer(int locationId, String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_volunteer"));
            pstmt.setString(1, userId);
            pstmt.setInt(2, locationId);
            pstmt.execute();
        }
    }

    @Override
    public List<User> getVolunteers(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_volunteers"));
            pstmt.setInt(1, locationId);
            ResultSet set = pstmt.executeQuery();
            List<User> users = new ArrayList<>();
            while(set.next())
                users.add(createUser(set, conn));
            return users;
        }
    }

    @Override
    public List<Locker> getLockers(int locationId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Locker> lockers = new ArrayList<>();
            String query = Resources.databaseProperties.getString("get_lockers_where_<?>");
            query = query.replace("<?>", "l.location_id = ?");
            PreparedStatement st = conn.prepareStatement(query);
            st.setInt(1, locationId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                //int lockerID = rs.getInt(Resources.databaseProperties.getString("locker_id"));
                int number = rs.getInt(Resources.databaseProperties.getString("locker_number"));

                Locker locker = new Locker();
                //locker.setId(lockerID);
                locker.setNumber(number);

                Location location = DBLocationDao.createLocation(rs, conn);
                locker.setLocation(location);

                lockers.add(locker);
            }
            return lockers;
        }
    }

    @Override
    public void deleteLocker(int locationId, int number) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locker"));
            pstmt.setInt(1, locationId);
            pstmt.setInt(2, number);
            pstmt.execute();
        }
    }

    @Override
    public void approveLocation(Location location, boolean approval) throws SQLException {
        this.updateLocation(location.getLocationId(), location);

        try (Connection conn = adb.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(Resources.databaseProperties.getString("approve_location"));
            statement.setBoolean(1, approval);
            statement.setInt(2, location.getLocationId());
            statement.execute();
        }
    }
    /**
     * Create a location out of a row in the ResultSet (prevent duplication of code)
     * @param rs the ResultSet for fetching the location
     * @param rsTags the ResultSet for fetching the tags
     * @return a generated location
     */
    private static Location createLocation(ResultSet rs, ResultSet rsTags, Pair<LocationStatus, String> status) throws SQLException {
        int locationId = rs.getInt(Resources.databaseProperties.getString("location_id"));
        String name = rs.getString(Resources.databaseProperties.getString("location_name"));
        int numberOfSeats = rs.getInt(Resources.databaseProperties.getString("location_number_of_seats"));
        int numberOfLockers = rs.getInt(Resources.databaseProperties.getString("location_number_of_lockers"));
        boolean forGroup = rs.getBoolean(Resources.databaseProperties.getString("location_forGroup"));
        String imageUrl = rs.getString(Resources.databaseProperties.getString("location_image_url"));
        Building building = DBBuildingDao.createBuilding(rs);
        Authority authority = DBAuthorityDao.createAuthority(rs);

        String descriptionDutch = rs.getString(Resources.databaseProperties.getString("location_description_dutch"));
        String descriptionEnglish = rs.getString(Resources.databaseProperties.getString("location_description_english"));

        List<LocationTag> assignedTags = new ArrayList<>();
        fillTagLists(assignedTags, rsTags);

        return new Location(locationId, name, numberOfSeats, numberOfLockers, imageUrl, authority,
                descriptionDutch, descriptionEnglish, building, forGroup, assignedTags, status);
    }

    private static void fillTagLists(List<LocationTag> assignedTags, ResultSet rsTags)
            throws SQLException {
        while (rsTags.next()) {
            LocationTag locationTag = DBTagsDao.createLocationTag(rsTags);
            assignedTags.add(locationTag);
        }
    }

    /**
     * create a location from the resultSet, where tags and status are automatically fetched too
     */
    public static Location createLocation(ResultSet rs, Connection conn) throws SQLException {
        ResultSet rsTags = DBLocationTagDao.getTagsForLocation(rs.getInt(Resources.databaseProperties.getString("location_id")), conn);
        int locationId = rs.getInt(Resources.databaseProperties.getString("location_id"));
        Pair<LocationStatus, String> status = DBCalendarPeriodDao.getStatus(locationId, conn);
        return createLocation(rs, rsTags, status);
    }

    public static Locker createLocker(ResultSet rs, Connection conn) throws SQLException {
        Locker l = new Locker();
        l.setNumber(rs.getInt(Resources.databaseProperties.getString("locker_reservation_locker_number")));
        Location location = DBLocationDao.createLocation(rs, conn);
        l.setLocation(location);
        return l;
    }

    // helper method for AddLocation
    // inserts lockers in the locker table
    private void insertLocker(int locationId, int number, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("insert_locker"));
        st.setInt(1, number);
        st.setInt(2, locationId);
        st.execute();
    }

    private void insertTags(int locationId, List<LocationTag> tags, Connection conn) throws SQLException {
        if (tags != null) {
            for (LocationTag tag : tags) {
                DBTagsDao.addTag(tag, conn);
                DBLocationTagDao.addTagToLocation(locationId, tag.getTagId(), conn);
            }
        }
    }

    private void prepareUpdateOrInsertLocationStatement(Location location, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, location.getName());
        pstmt.setInt(2, location.getNumberOfSeats());
        pstmt.setInt(3, location.getNumberOfLockers());
        pstmt.setString(4, location.getImageUrl());
        pstmt.setInt(5, location.getAuthority().getAuthorityId());
        pstmt.setInt(6, location.getBuilding().getBuildingId());
        pstmt.setString(7, location.getDescriptionDutch());
        pstmt.setString(8, location.getDescriptionEnglish());
        pstmt.setBoolean(9, location.getForGroup());
    }

    private void deleteLocker(int locationId, int number, Connection conn) throws SQLException {
        deleteLockerReservation(locationId, number, conn);

        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locker"));
        pstmt.setInt(1, locationId);
        pstmt.setInt(2, number);
        pstmt.execute();
    }

    private void deleteLockerReservation(int locationId, int number, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locker_reservation"));
        pstmt.setInt(1, locationId);
        pstmt.setInt(2, number);
        pstmt.execute();
    }
}

