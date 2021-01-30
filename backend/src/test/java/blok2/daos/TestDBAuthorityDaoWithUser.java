package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestDBAuthorityDaoWithUser extends BaseTest {

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IAccountDao accountDao;

    @Autowired ILocationDao locationDao;

    @Autowired IBuildingDao buildingDao;

    private Authority testAuthority;
    private Authority testAuthority2;
    private User testUser;
    private Location testLocation1;
    private Location testLocation2;
    private Building testBuilding;

    @Override
    public void populateDatabase() throws SQLException {
        testAuthority = TestSharedMethods.insertTestAuthority(authorityDao);
        testAuthority2 = TestSharedMethods.insertTestAuthority2(authorityDao);

        testUser = TestSharedMethods.adminTestUser();

        testBuilding = TestSharedMethods.testBuilding();
        testBuilding = buildingDao.addBuilding(testBuilding);

        testLocation1 = TestSharedMethods.testLocation(testAuthority.clone(), testBuilding);
        testLocation2 = TestSharedMethods.testLocation2(testAuthority2.clone(), testBuilding);

        accountDao.directlyAddUser(testUser);
        authorityDao.addUserToAuthority(testUser.getAugentID(), testAuthority.getAuthorityId());
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);
    }

    @Test
    public void getAuthoritiesFromUser() throws SQLException {
        List<Authority> authorities = authorityDao.getAuthoritiesFromUser(testUser.getAugentID());
        Assert.assertEquals(1, authorities.size());
        Assert.assertTrue(authorities.contains(testAuthority));
    }

    @Test
    public void getUsersFromAuthority() throws SQLException {
        List<User> users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(1, users.size());
        Assert.assertTrue(users.contains(testUser));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
    }

    @Test
    public void addAndRemoveUserFromAuthority() throws SQLException {
        //todo redo testUsers in TestSharedMethods so they are inserted directly
        User user = TestSharedMethods.adminTestUser();
        user.setAugentID("000010");
        user.setMail("newtestMail@ugent.be");
        accountDao.directlyAddUser(user);
        authorityDao.addUserToAuthority(user.getAugentID(), testAuthority.getAuthorityId());
        List<User> users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(2, users.size());
        Assert.assertTrue(users.contains(user));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
        authorityDao.deleteUserFromAuthority(user.getAugentID(), testAuthority.getAuthorityId());
        users = authorityDao.getUsersFromAuthority(testAuthority.getAuthorityId());
        Assert.assertEquals(1, users.size());
        Assert.assertFalse(users.contains(user));
        users = authorityDao.getUsersFromAuthority(testAuthority2.getAuthorityId());
        Assert.assertTrue(users.isEmpty());
    }

    @Test
    public void getLocationsInAuthoritiesOfUser() throws SQLException {
        // testUser was added to testAuthority in populateDatabase() so expect testLocation1 to be manageable
        List<Location> actual = authorityDao.getLocationsInAuthoritiesOfUser(testUser.getAugentID());

        List<Location> expected = new ArrayList<>();
        expected.add(testLocation1);

        Assert.assertEquals(expected, actual);

        // Add testUser to testAuthority2 and expect testLocation2 to be manageable as well
        authorityDao.addUserToAuthority(testUser.getAugentID(), testAuthority2.getAuthorityId());
        actual = authorityDao.getLocationsInAuthoritiesOfUser(testUser.getAugentID());

        expected.add(testLocation2);

        actual.sort(Comparator.comparing(Location::getName));
        expected.sort(Comparator.comparing(Location::getName));
        Assert.assertEquals(expected, actual);
    }
}
