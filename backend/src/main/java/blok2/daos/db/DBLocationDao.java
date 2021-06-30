package blok2.daos.db;

import blok2.daos.IUserDao;
import blok2.daos.repositories.LocationRepository;
import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.helpers.Resources;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

import static blok2.daos.db.DBCalendarPeriodDao.getCurrentTimeslot;

@Service
public class DBLocationDao extends DAO {

    IUserDao userDao;
    LocationRepository locationRepository;

    @Autowired
    public DBLocationDao(IUserDao userDao, LocationRepository locationRepository) {
        this.userDao = userDao;
        this.locationRepository = locationRepository;
    }

    /**
     * create a location from the resultSet, where tags and status are automatically fetched too
     */
    public static Location createLocation(ResultSet rs, Connection conn) throws SQLException {
        ResultSet rsTags = DBLocationTagDao.getTagsForLocation(rs.getInt(Resources.databaseProperties.getString("location_id")), conn);
        int locationId = rs.getInt(Resources.databaseProperties.getString("location_id"));
        Pair<LocationStatus, String> status = DBCalendarPeriodDao.getStatus(locationId, conn);
        Timeslot timeslot = getCurrentTimeslot(locationId, conn);
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
        while (rsTags.next()) {
            LocationTag locationTag = DBTagsDao.createLocationTag(rsTags);
            assignedTags.add(locationTag);
        }

        return new Location(locationId, name, numberOfSeats, numberOfLockers, imageUrl, authority,
                descriptionDutch, descriptionEnglish, building, forGroup, assignedTags, status, timeslot);
    }

}

