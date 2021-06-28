package blok2.daos.db;

import blok2.daos.IAccountDao;
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
public class DBLocationDao extends DAO {

    IAccountDao accountDao;
    IScannerLocationDao scannerLocationDao;
    LocationRepository locationRepository;

    @Autowired
    public DBLocationDao(IAccountDao accountDao, IScannerLocationDao scannerLocationDao, LocationRepository locationRepository) {
        this.accountDao = accountDao;
        this.scannerLocationDao = scannerLocationDao;
        this.locationRepository = locationRepository;
    }

    public void addVolunteer(int locationId, String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("add_volunteer"));
            pstmt.setString(1, userId);
            pstmt.setInt(2, locationId);
            pstmt.execute();
        }
    }

    public void deleteVolunteer(int locationId, String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(Resources.databaseProperties.getString("delete_volunteer"));
            pstmt.setString(1, userId);
            pstmt.setInt(2, locationId);
            pstmt.execute();
        }
    }

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

}

