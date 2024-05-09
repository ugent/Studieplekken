package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.database.dao.IAuthorityDao;
import blok2.database.dao.IBuildingDao;
import blok2.database.dao.ILocationDao;
import blok2.database.dao.IUserDao;
import blok2.exceptions.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.location.Location;
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
    private IUserDao userDao;

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

        testUserStudent = TestSharedMethods.studentTestUser();
        testUserAdmin = TestSharedMethods.adminTestUser();

        // Add test objects to database
        testLocation1 = locationDao.addLocation(TestSharedMethods.testLocation(testAuthority.clone(), testBuilding.clone()));
        testLocation2 = locationDao.addLocation(TestSharedMethods.testLocation2(testAuthority.clone(), testBuilding.clone()));

        userDao.addUser(testUserStudent);
        userDao.addUser(testUserAdmin);
    }

    @Test(expected = NoSuchDatabaseObjectException.class)
    public void deleteAuthorityCascadeTest() {
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

        // And the locations must have been deleted on cascade
        Assert.assertEquals(0, authorityDao.getLocationsInAuthority(testAuthority.getAuthorityId()).size());

        // And the authorities should have been removed from the users
        Assert.assertEquals(0, authorityDao.getAuthoritiesFromUser(testUserAdmin.getUserId()).size());
        Assert.assertEquals(0, authorityDao.getAuthoritiesFromUser(testUserStudent.getUserId()).size());

        // Authority must be deleted
        authorityDao.getAuthorityByAuthorityId(testAuthority.getAuthorityId()); // excpected to throw NoSuchDatabaseObjectException
    }
}
