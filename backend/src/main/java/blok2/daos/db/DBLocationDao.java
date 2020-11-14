package blok2.daos.db;

import blok2.daos.IAccountDao;
import blok2.daos.ILocationDao;
import blok2.daos.IScannerLocationDao;
import blok2.helpers.Resources;
import blok2.helpers.date.CustomDate;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public void addLocation(Location location) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            addLocationAsTransaction(location, conn);
        }
    }

    private void addLocation(Location location, Connection conn) throws SQLException {

        // insert location into the database
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("insert_location"));
        prepareUpdateOrInsertLocationStatement(location, pstmt);
        pstmt.executeUpdate();

        insertTags(location.getName(), location.getAssignedTags(), conn);

        // insert the lockers corresponding to the location into the database
        for (int i = 0; i < location.getNumberOfLockers(); i++) {
            insertLocker(location.getName(), i, conn);
        }
    }

    private void addLocationAsTransaction(Location location, Connection conn) throws SQLException {
        try {
            conn.setAutoCommit(false);
            addLocation(location, conn);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public Location getLocation(String name) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getLocation(name, conn);
        }
    }

    public static Location getLocation(String locationName, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_location"));
        pstmt.setString(1, locationName);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return createLocation(rs, conn);
        }
        return null;
    }

    @Override
    public void updateLocation(String locationName, Location location) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            try {
                conn.setAutoCommit(false);

                Location oldLocation = getLocation(locationName, conn);

                if (oldLocation == null)
                    return;

                // Update location
                PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("update_location"));
                // set ...
                prepareUpdateOrInsertLocationStatement(location, pstmt);
                // where ...
                pstmt.setString(10, locationName);
                pstmt.execute();

                // Update lockers if necessary
                if (oldLocation.getNumberOfLockers() != location.getNumberOfLockers()) {
                    updateNumberOfLockers(location.getName(), oldLocation.getNumberOfLockers()
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

    private void updateNumberOfLockers(String locationName, int from, int to, Connection conn) throws SQLException {
        if (from > to) {
            decreaseNumberOfLockers(locationName, from, to, conn);
        } else {
            increaseNumberOfLockers(locationName, from, to, conn);
        }
    }

    private void increaseNumberOfLockers(String locationName, int from, int to, Connection conn) throws SQLException {
        for (int i = from; i < to; i++) {
            insertLocker(locationName, i, conn);
        }
    }

    private void decreaseNumberOfLockers(String locationName, int from, int to, Connection conn) throws SQLException {
        for (int i = from - 1; i >= to; i--) {
            deleteLocker(locationName, i, conn);
        }
    }

    @Override
    public void deleteLocation(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_location"));
            pstmt.setString(1, locationName);
            pstmt.execute();
        }
    }

    @Override
    public List<Locker> getLockers(String locationName) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            List<Locker> lockers = new ArrayList<>();
            String query = Resources.databaseProperties.getString("get_lockers_where_<?>");
            query = query.replace("<?>", "l.location_name = ?");
            PreparedStatement st = conn.prepareStatement(query);
            st.setString(1, locationName);
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
    public void deleteLocker(String locationName, int number) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locker"));
            pstmt.setString(1, locationName);
            pstmt.setInt(2, number);
            pstmt.execute();
        }
    }

    private void deleteLocker(String locationName, int number, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_locker"));
        pstmt.setString(1, locationName);
        pstmt.setInt(2, number);
        pstmt.execute();
    }

    @Override
    public Map<String, Integer> getCountOfReservations(CustomDate date) throws SQLException {
        HashMap<String, Integer> count = new HashMap<>();

        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("count_location_reservations_on_date"));
            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                int c = rs.getInt(2);
                count.put(name, c);
            }

            return count;
        }
    }

    @Override
    public void approveLocation(Location location, boolean approval) throws SQLException {
        this.updateLocation(location.getName(), location);

        try (Connection conn = adb.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(Resources.databaseProperties.getString("approve_location"));
            statement.setBoolean(1, approval);
            statement.setString(2, location.getName());
            statement.execute();
        }
    }
    /**
     * Create a location out of a row in the ResultSet (prevent duplication of code)
     * @param rs the ResultSet for fetching the location
     * @param rsTags the ResultSet for fetching the tags
     * @return a generated location
     */
    public static Location createLocation(ResultSet rs, ResultSet rsTags) throws SQLException {
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

        return new Location(name, numberOfSeats, numberOfLockers, imageUrl, authority,
                descriptionDutch, descriptionEnglish, building, forGroup, assignedTags);
    }

    private static void fillTagLists(List<LocationTag> assignedTags, ResultSet rsTags)
            throws SQLException {
        while (rsTags.next()) {
            LocationTag locationTag = DBTagsDao.createLocationTag(rsTags);
            assignedTags.add(locationTag);
        }
    }

    /**
     * create a location from the resultset, where tags are automatically fetched too
     */
    public static Location createLocation(ResultSet rs, Connection conn) throws SQLException {
        ResultSet rsTags = DBLocationTagDao.getTagsForLocation(rs.getString(Resources.databaseProperties.getString("location_name")), conn);
        return createLocation(rs, rsTags);
    }


    public static Locker createLocker(ResultSet rs, Connection conn) throws SQLException {
        Locker l = new Locker();
        l.setNumber(rs.getInt(Resources.databaseProperties.getString("locker_number")));
        Location location = DBLocationDao.createLocation(rs, conn);
        l.setLocation(location);
        return l;
    }

    // helper method for AddLocation
    // inserts lockers in the locker table
    private void insertLocker(String locationName, int number, Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(Resources.databaseProperties.getString("insert_locker"));
        st.setInt(1, number);
        st.setString(2, locationName);
        st.execute();
    }

    private void insertTags(String locationName, List<LocationTag> tags, Connection conn) throws SQLException {
        if (tags != null) {
            for (LocationTag tag : tags) {
                DBTagsDao.addTag(tag, conn);
                DBLocationTagDao.addTagToLocation(locationName, tag.getTagId(), conn);
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
}

