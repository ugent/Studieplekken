package blok2.daos.services;

import blok2.daos.IVolunteerDao;
import blok2.daos.db.DAO;
import blok2.daos.db.DBLocationDao;
import blok2.daos.orm.LocationRepository;
import blok2.helpers.Resources;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VolunteerService extends DAO implements IVolunteerDao {

    private final LocationRepository locationRepository;
    private final DBLocationDao dbLocationDao; // TODO: delete after all implementation

    public VolunteerService(LocationRepository locationRepository, DBLocationDao dbLocationDao) {
        this.locationRepository = locationRepository;
        this.dbLocationDao = dbLocationDao;
    }

    @Override
    public List<User> getVolunteers(int locationId) throws SQLException {
        return dbLocationDao.getVolunteers(locationId);
    }

    @Override
    public List<Location> getVolunteeredLocations(String userId) throws SQLException {
        try (Connection conn = adb.getConnection()) {
            return getLocationsOfVolunteer(userId, conn);
        }
    }

    public static List<Location> getLocationsOfVolunteer(String userId, Connection conn) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                Resources.databaseProperties.getString("get_locations_of_volunteer"));
        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();

        List<Location> locations = new ArrayList<>();
        while (rs.next()) {
            locations.add(DBLocationDao.createLocation(rs, conn));
        }
        return locations;
    }

    @Override
    public void addVolunteer(int locationId, String userId) throws SQLException {
        dbLocationDao.addVolunteer(locationId, userId);
    }

    @Override
    public void deleteVolunteer(int locationId, String userId) throws SQLException {
        dbLocationDao.deleteVolunteer(locationId, userId);
    }

}
