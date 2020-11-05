package blok2.daos.cascade;

import blok2.daos.*;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.reservables.Location;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestCascadeInDBAuthorityDao extends TestDao {

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    @Autowired
    private ILocationDao locationDao;

    private Authority testAuthority;
    private Location testLocation1;
    private Location testLocation2;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        testAuthority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation1 = TestSharedMethods.testLocation(testAuthority.clone(), testBuilding.clone());
        testLocation2 = TestSharedMethods.testLocation2(testAuthority.clone(), testBuilding.clone());

        // Add test objects to database
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);
    }

    @Test
    public void deleteAuthorityCascadeTest() throws SQLException {
        // Test locations are present in db
        List<Location> locations = authorityDao.getLocationsInAuthority(testAuthority.getAuthorityId());

        List<Location> expectedLocations = new ArrayList<>();
        expectedLocations.add(testLocation1);
        expectedLocations.add(testLocation2);

        locations.sort(Comparator.comparing(Location::getName));
        expectedLocations.sort(Comparator.comparing(Location::getName));

        Assert.assertEquals(expectedLocations, locations);

        // Delete the building
        authorityDao.deleteAuthority(testAuthority.getAuthorityId());

        // Building must be deleted
        Assert.assertNull(authorityDao.getAuthorityByAuthorityId(testAuthority.getAuthorityId()));

        // And the locations must have been deleted on cascade
        Assert.assertEquals(0, authorityDao.getLocationsInAuthority(testAuthority.getAuthorityId()).size());
    }
}
