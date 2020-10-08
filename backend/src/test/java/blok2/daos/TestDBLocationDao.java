package blok2.daos;

import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

public class TestDBLocationDao extends TestDao {

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    private Location testLocation;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.getAuthorityId());

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @Test
    public void addLocationTest() throws SQLException {
        Location l = locationDao.getLocation(testLocation.getName());
        Assert.assertEquals("addLocation", testLocation, l);

        locationDao.deleteLocation(testLocation.getName());
        l = locationDao.getLocation(testLocation.getName());
        Assert.assertNull("addLocation, remove added test location", l);
    }

    @Test
    public void lockersTest() throws SQLException {
        List<Locker> lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("lockersTest, check size getLockers"
                , testLocation.getNumberOfLockers(), lockers.size());

        if (testLocation.getNumberOfLockers() > 0) {
            for (Locker l : lockers) {
                Assert.assertEquals("lockersTest, check location of each locker"
                        , testLocation, l.getLocation());
            }
        }
    }
}
