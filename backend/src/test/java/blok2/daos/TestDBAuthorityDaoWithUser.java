package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.database.dao.IAuthorityDao;
import blok2.database.dao.IBuildingDao;
import blok2.database.dao.ILocationDao;
import blok2.database.dao.IUserDao;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.location.Location;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestDBAuthorityDaoWithUser extends BaseTest {

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IUserDao userDao;

    @Autowired
    ILocationDao locationDao;

    @Autowired
    IBuildingDao buildingDao;

    private Authority testAuthority;
    private Authority testAuthority2;
    private User testUser;
    private Location testLocation1;
    private Location testLocation2;

    @Override
    public void populateDatabase() {
        testAuthority = TestSharedMethods.insertTestAuthority(authorityDao);
        testAuthority2 = TestSharedMethods.insertTestAuthority2(authorityDao);

        testUser = TestSharedMethods.adminTestUser();

        Building testBuilding = TestSharedMethods.testBuilding();
        testBuilding = buildingDao.addBuilding(testBuilding);

        testLocation1 = TestSharedMethods.testLocation(testAuthority.clone(), testBuilding);
        testLocation2 = TestSharedMethods.testLocation2(testAuthority2.clone(), testBuilding);

        userDao.addUser(testUser);
        authorityDao.addUserToAuthority(testUser.getUserId(), testAuthority.getAuthorityId());
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);
    }

    @Test
    public void getAuthoritiesFromUser() {
        List<Authority> authorities = authorityDao.getAuthoritiesFromUser(testUser.getUserId());
        Assert.assertEquals(1, authorities.size());
        Assert.assertTrue(authorities.contains(testAuthority));
    }

    @Test
    public void getUsersFromAuthority() {
        List<User> users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(1, users.size());
        Assert.assertTrue(users.contains(testUser));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
    }

    @Test
    public void addAndRemoveUserFromAuthority() {
        //todo redo testUsers in TestSharedMethods so they are inserted directly
        User user = TestSharedMethods.adminTestUser();
        user.setUserId("000010");
        user.setMail("newtestMail@ugent.be");
        userDao.addUser(user);
        authorityDao.addUserToAuthority(user.getUserId(), testAuthority.getAuthorityId());
        List<User> users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(2, users.size());
        Assert.assertTrue(users.contains(user));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
        authorityDao.deleteUserFromAuthority(user.getUserId(), testAuthority.getAuthorityId());
        users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(1, users.size());
        Assert.assertFalse(users.contains(user));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
    }

    @Test
    public void getLocationsInAuthoritiesOfUser() {
        // testUser was added to testAuthority in populateDatabase() so expect testLocation1 to be manageable
        List<Location> actual = authorityDao.getLocationsInAuthoritiesOfUser(testUser.getUserId());

        List<Location> expected = new ArrayList<>();
        expected.add(testLocation1);

        Assert.assertEquals(expected, actual);

        // Add testUser to testAuthority2 and expect testLocation2 to be manageable as well
        authorityDao.addUserToAuthority(testUser.getUserId(), testAuthority2.getAuthorityId());
        actual = authorityDao.getLocationsInAuthoritiesOfUser(testUser.getUserId());

        expected.add(testLocation2);

        actual.sort(Comparator.comparing(Location::getName));
        expected.sort(Comparator.comparing(Location::getName));
        Assert.assertEquals(expected, actual);
    }
}
