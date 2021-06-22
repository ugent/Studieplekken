package blok2.daos.db;

import blok2.daos.IAccountDao;
import blok2.daos.ILocationDao;
import blok2.daos.IScannerLocationDao;
import blok2.daos.orm.LocationRepository;
import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.helpers.Resources;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

import static blok2.daos.db.DBCalendarPeriodDao.getCurrentTimeslot;
import static blok2.daos.db.DBAccountDao.createUser;

@Service
public class DBLocationDao extends DAO implements ILocationDao {

    IAccountDao accountDao;
    IScannerLocationDao scannerLocationDao;
    LocationRepository locationRepository;

    @Autowired
    public DBLocationDao(IAccountDao accountDao, IScannerLocationDao scannerLocationDao, LocationRepository locationRepository) {
        this.accountDao = accountDao;
        this.scannerLocationDao = scannerLocationDao;
        this.locationRepository = locationRepository;
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
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("get_volunteers_of_location"));
            pstmt.setInt(1, locationId);
            ResultSet set = pstmt.executeQuery();
            List<User> users = new ArrayList<>();
            while(set.next())
                users.add(createUser(set, conn));
            return users;
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
    private static Location createLocation(ResultSet rs, ResultSet rsTags, Pair<LocationStatus, String> status, Timeslot timeslot) throws SQLException {
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
                descriptionDutch, descriptionEnglish, building, forGroup, assignedTags, status, timeslot);
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
        Timeslot timeslot = getCurrentTimeslot(locationId, conn);

        return createLocation(rs, rsTags, status, timeslot);
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

}

