package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

@FlywayTest
public class TestDBLocationDao extends BaseTest {

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    private Location testLocation;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);

        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @FlywayTest
    @Test
    public void addLocationTest() throws SQLException {
        Location l = locationDao.getLocationByName(testLocation.getName());
        Assert.assertEquals("addLocation", testLocation, l);

        locationDao.deleteLocation(testLocation.getLocationId());
        l = locationDao.getLocationByName(testLocation.getName());
        Assert.assertNull("addLocation, remove added test location", l);
    }

    @FlywayTest
    @Test
    public void lockersTest() throws SQLException {
        List<Locker> lockers = locationDao.getLockers(testLocation.getLocationId());
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
