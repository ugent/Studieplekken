package blok2.daos;

import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDBLocationDao {

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    private Location testLocation;
    private Authority authority;

    @Before
    public void setup() throws SQLException {
        // Setup test objects
        authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.getAuthorityId());

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database
        locationDao.deleteLocation(testLocation.getName());
        authorityDao.deleteAuthority(authority.getAuthorityId());
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
