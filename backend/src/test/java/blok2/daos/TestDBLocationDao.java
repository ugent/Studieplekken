package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.helpers.exceptions.NoSuchLocationException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.reservables.Location;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


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
    public void populateDatabase() {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);

        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @FlywayTest
    @Test
    public void addLocationTest() {
        Location l = locationDao.getLocationByName(testLocation.getName());
        Assert.assertEquals("addLocation", testLocation, l);

        locationDao.deleteLocation(testLocation.getLocationId());
        try {
            locationDao.getLocationByName(testLocation.getName());
        } catch (NoSuchLocationException ignore) {
            Assert.assertTrue("Location must be deleted and thus a NoSuchLocationException should have been thrown.", true);
        }
    }

}
