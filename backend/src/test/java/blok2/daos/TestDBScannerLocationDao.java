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

public class TestDBScannerLocationDao extends BaseTest {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IScannerLocationDao scannerLocationDao;

    @Autowired
    private IBuildingDao buildingDao;

    private User testUser1;
    private User testUser2;

    private Location testLocation1;
    private Location testLocation2;

    private List<User> expectedUsersOfLocation1;
    private List<User> expectedUsersOfLocation2;

    private List<Location> expectedLocationsOfUser1;
    private List<Location> expectedLocationsOfUser2;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        testUser1 = TestSharedMethods.adminTestUser();
        testUser2 = TestSharedMethods.studentTestUser();

        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);

        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation1 = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        testLocation2 = TestSharedMethods.testLocation2(authority.clone(), testBuilding);

        // Add test objects to database
        TestSharedMethods.addTestUsers(accountDao, testUser1, testUser2);
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);

        scannerLocationDao.addScannerLocation(testLocation1.getLocationId(), testUser1.getAugentID());
        scannerLocationDao.addScannerLocation(testLocation1.getLocationId(), testUser2.getAugentID());
        expectedUsersOfLocation1 = new ArrayList<>();
        expectedUsersOfLocation1.add(testUser1);
        expectedUsersOfLocation1.add(testUser2);

        scannerLocationDao.addScannerLocation(testLocation2.getLocationId(), testUser1.getAugentID());
        expectedUsersOfLocation2 = new ArrayList<>();
        expectedUsersOfLocation2.add(testUser1);

        expectedLocationsOfUser1 = new ArrayList<>();
        expectedLocationsOfUser1.add(testLocation1);
        expectedLocationsOfUser1.add(testLocation2);

        expectedLocationsOfUser2 = new ArrayList<>();
        expectedLocationsOfUser2.add(testLocation1);
    }

    @Test
    public void gettersTest() throws SQLException {
        // sort the expected lists
        expectedLocationsOfUser1.sort(Comparator.comparing(Location::getName));
        expectedLocationsOfUser2.sort(Comparator.comparing(Location::getName));
        expectedUsersOfLocation1.sort(Comparator.comparing(User::getAugentID));
        expectedUsersOfLocation2.sort(Comparator.comparing(User::getAugentID));

        // get the actual lists
        List<User> usersOnLocation1 = scannerLocationDao.getScannersOnLocation(testLocation1.getLocationId());
        List<User> usersOnLocation2 = scannerLocationDao.getScannersOnLocation(testLocation2.getLocationId());
        List<Location> locationsOfUser1 = scannerLocationDao.getLocationsToScanOfUser(testUser1.getAugentID());
        List<Location> locationsOfUser2 = scannerLocationDao.getLocationsToScanOfUser(testUser2.getAugentID());

        // sort the actual lists
        locationsOfUser1.sort(Comparator.comparing(Location::getName));
        locationsOfUser2.sort(Comparator.comparing(Location::getName));
        usersOnLocation1.sort(Comparator.comparing(User::getAugentID));
        usersOnLocation2.sort(Comparator.comparing(User::getAugentID));

        // assert expected with actual lists
        Assert.assertEquals("gettersTest, getScannersOnLocation on location 1",
                expectedUsersOfLocation1, usersOnLocation1);
        Assert.assertEquals("gettersTest, getScannersOnLocation on location 2",
                expectedUsersOfLocation2, usersOnLocation2);
        Assert.assertEquals("gettersTest, getLocationsToScanOfUser of user 1",
                expectedLocationsOfUser1, locationsOfUser1);
        Assert.assertEquals("gettersTest, getLocationsToScanOfUser of user 2",
                expectedLocationsOfUser2, locationsOfUser2);
    }

    @Test
    public void deleteScannerLocationTest() throws SQLException {
        scannerLocationDao.deleteScannerLocation(testLocation1.getLocationId(), testUser2.getAugentID());
        List<Location> locationsOfUser = scannerLocationDao.getLocationsToScanOfUser(testUser2.getAugentID());
        Assert.assertEquals("deleteScannerLocationTest, delete location1 of testUser2",
                0, locationsOfUser.size());

        scannerLocationDao.deleteScannerLocation(testLocation1.getLocationId(), testUser1.getAugentID());
        locationsOfUser = scannerLocationDao.getLocationsToScanOfUser(testUser1.getAugentID());
        Assert.assertEquals("deleteScannerLocationTest, delete location1 of testUser1",
                1, locationsOfUser.size());

        scannerLocationDao.deleteScannerLocation(testLocation2.getLocationId(), testUser1.getAugentID());
        locationsOfUser = scannerLocationDao.getLocationsToScanOfUser(testUser1.getAugentID());
        Assert.assertEquals("deleteScannerLocationTest, delete location1 of testUser1",
                0, locationsOfUser.size());
    }

    @Test
    public void deleteAllScannersOfLocationTest() throws SQLException {
        scannerLocationDao.deleteAllScannersOfLocation(testLocation1.getLocationId());
        List<User> usersOfLocation = scannerLocationDao.getScannersOnLocation(testLocation1.getLocationId());
        Assert.assertEquals("deleteAllScannersOfLocationTest, delete all from testLocation1",
                0, usersOfLocation.size());

        scannerLocationDao.deleteAllScannersOfLocation(testLocation2.getLocationId());
        usersOfLocation = scannerLocationDao.getScannersOnLocation(testLocation2.getLocationId());
        Assert.assertEquals("deleteAllScannersOfLocationTest, delete all from testLocation2",
                0, usersOfLocation.size());
    }

    @Test
    public void deleteAllLocationsOfScannerTest() throws SQLException {
        scannerLocationDao.deleteAllLocationsOfScanner(testUser1.getAugentID());
        List<Location> locationsOfUser = scannerLocationDao.getLocationsToScanOfUser(testUser1.getAugentID());
        Assert.assertEquals("deleteAllLocationsOfScannerTest, delete all locations of testUser1",
                0, locationsOfUser.size());

        scannerLocationDao.deleteAllLocationsOfScanner(testUser2.getAugentID());
        locationsOfUser = scannerLocationDao.getLocationsToScanOfUser(testUser2.getAugentID());
        Assert.assertEquals("deleteAllLocationsOfScannerTest, delete all locations of testUser2",
                0, locationsOfUser.size());
    }

    @Test
    public void isUserAllowedToScanTest() throws SQLException {
        Assert.assertTrue("isUserAllowedToScanTest, testUser1 -> testLocation1",
                scannerLocationDao.isUserAllowedToScan(testUser1.getAugentID(), testLocation1.getLocationId()));
        Assert.assertTrue("isUserAllowedToScanTest, testUser1 -> testLocation2",
                scannerLocationDao.isUserAllowedToScan(testUser1.getAugentID(), testLocation2.getLocationId()));
        Assert.assertTrue("isUserAllowedToScanTest, testUser2 -> testLocation1",
                scannerLocationDao.isUserAllowedToScan(testUser2.getAugentID(), testLocation1.getLocationId()));
    }
}
