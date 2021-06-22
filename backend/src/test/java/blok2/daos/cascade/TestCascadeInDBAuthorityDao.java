package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TestCascadeInDBAuthorityDao extends BaseTest {

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAccountDao accountDao;

    private Authority testAuthority;
    private Location testLocation1;
    private Location testLocation2;
    private User testUserStudent;
    private User testUserAdmin;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        testAuthority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation1 = TestSharedMethods.testLocation(testAuthority.clone(), testBuilding.clone());
        testLocation2 = TestSharedMethods.testLocation2(testAuthority.clone(), testBuilding.clone());

        testUserStudent = TestSharedMethods.studentTestUser();
        testUserAdmin = TestSharedMethods.adminTestUser();


        // Add test objects to database
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);

        accountDao.directlyAddUser(testUserStudent);
        accountDao.directlyAddUser(testUserAdmin);
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

        // Test adding users to authority
        authorityDao.addUserToAuthority(testUserStudent.getUserId(), testAuthority.getAuthorityId());
        authorityDao.addUserToAuthority(testUserAdmin.getUserId(), testAuthority.getAuthorityId());

        List<User> users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        List<User> expectedUsers = new ArrayList<>(Arrays.asList(testUserAdmin, testUserStudent));

        users.sort(Comparator.comparing(User::getUserId));
        expectedUsers.sort(Comparator.comparing(User::getUserId));

        Assert.assertEquals(expectedUsers, users);


        // Delete the authority
        authorityDao.deleteAuthority(testAuthority.getAuthorityId());

        // Authority must be deleted
        Assert.assertNull(authorityDao.getAuthorityByAuthorityId(testAuthority.getAuthorityId()));

        // And the locations must have been deleted on cascade
        Assert.assertEquals(0, authorityDao.getLocationsInAuthority(testAuthority.getAuthorityId()).size());

        // And the authorities should have been removed from the users
        Assert.assertEquals(0, authorityDao.getAuthoritiesFromUser(testUserAdmin.getUserId()).size());
        Assert.assertEquals(0, authorityDao.getAuthoritiesFromUser(testUserStudent.getUserId()).size());
    }
}
