package blok2.daos.cascade;

import blok2.TestSharedMethods;
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

public class TestCascadeInDBBuildingsDao extends TestDao {

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    @Autowired
    private ILocationDao locationDao;

    private Building testBuilding;
    private Location testLocation1;
    private Location testLocation2;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation1 = TestSharedMethods.testLocation(authority.clone(), testBuilding.clone());
        testLocation2 = TestSharedMethods.testLocation2(authority.clone(), testBuilding.clone());

        // Add test objects to database
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);
        locationDao.approveLocation(testLocation1, true);
        locationDao.approveLocation(testLocation2, true);

    }

    @Test
    public void deleteBuildingCascadeTest() throws SQLException {
        // Test locations are present in db
        List<Location> locations = buildingDao.getLocationsInBuilding(testBuilding.getBuildingId());

        List<Location> expectedLocations = new ArrayList<>();
        expectedLocations.add(testLocation1);
        expectedLocations.add(testLocation2);

        locations.sort(Comparator.comparing(Location::getName));
        expectedLocations.sort(Comparator.comparing(Location::getName));

        Assert.assertEquals(expectedLocations, locations);

        // Delete the building
        buildingDao.deleteBuilding(testBuilding.getBuildingId());

        // Building must be deleted
        Assert.assertNull(buildingDao.getBuildingById(testBuilding.getBuildingId()));

        // And the locations must have been deleted on cascade
        Assert.assertEquals(0, buildingDao.getLocationsInBuilding(testBuilding.getBuildingId()).size());
    }

}
