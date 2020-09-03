package blok2.daos;

import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.users.User;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDBScannerLocationDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IScannerLocationDao scannerLocationDao;

    private User testUser1;
    private User testUser2;

    private Authority authority;
    private Location testLocation1;
    private Location testLocation2;

    private List<User> expectedUsersOfLocation1;
    private List<User> expectedUsersOfLocation2;

    private List<Location> expectedLocationsOfUser1;
    private List<Location> expectedLocationsOfUser2;

    @Before
    public void setup() throws SQLException {
        // Setup test objects
        testUser1 = TestSharedMethods.employeeAdminTestUser();
        testUser2 = TestSharedMethods.studentEmployeeTestUser();

        authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation1 = TestSharedMethods.testLocation(authority.getAuthorityId());
        testLocation2 = TestSharedMethods.testLocation2(authority.getAuthorityId());

        // Add test objects to database
        TestSharedMethods.addTestUsers(accountDao, testUser1, testUser2);
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);

        scannerLocationDao.addScannerLocation(testLocation1.getName(), testUser1.getAugentID());
        scannerLocationDao.addScannerLocation(testLocation1.getName(), testUser2.getAugentID());
        expectedUsersOfLocation1 = new ArrayList<>();
        expectedUsersOfLocation1.add(testUser1);
        expectedUsersOfLocation1.add(testUser2);

        scannerLocationDao.addScannerLocation(testLocation2.getName(), testUser1.getAugentID());
        expectedUsersOfLocation2 = new ArrayList<>();
        expectedUsersOfLocation2.add(testUser1);

        expectedLocationsOfUser1 = new ArrayList<>();
        expectedLocationsOfUser1.add(testLocation1);
        expectedLocationsOfUser1.add(testLocation2);

        expectedLocationsOfUser2 = new ArrayList<>();
        expectedLocationsOfUser2.add(testLocation1);
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database (ScannerLocations are deleted by cascade)
        locationDao.deleteLocation(testLocation2.getName());
        locationDao.deleteLocation(testLocation1.getName());
        TestSharedMethods.removeTestUsers(accountDao, testUser2, testUser1);
        authorityDao.deleteAuthority(authority.getAuthorityId());
    }

    @Test
    public void gettersTest() throws SQLException {
        // sort the expected lists
        expectedLocationsOfUser1.sort(Comparator.comparing(Location::getName));
        expectedLocationsOfUser2.sort(Comparator.comparing(Location::getName));
        expectedUsersOfLocation1.sort(Comparator.comparing(User::getAugentID));
        expectedUsersOfLocation2.sort(Comparator.comparing(User::getAugentID));

        // get the actual lists
        List<User> usersOnLocation1 = scannerLocationDao.getScannersOnLocation(testLocation1.getName());
        List<User> usersOnLocation2 = scannerLocationDao.getScannersOnLocation(testLocation2.getName());
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
        scannerLocationDao.deleteScannerLocation(testLocation1.getName(), testUser2.getAugentID());
        List<Location> locationsOfUser = scannerLocationDao.getLocationsToScanOfUser(testUser2.getAugentID());
        Assert.assertEquals("deleteScannerLocationTest, delete location1 of testUser2",
                0, locationsOfUser.size());

        scannerLocationDao.deleteScannerLocation(testLocation1.getName(), testUser1.getAugentID());
        locationsOfUser = scannerLocationDao.getLocationsToScanOfUser(testUser1.getAugentID());
        Assert.assertEquals("deleteScannerLocationTest, delete location1 of testUser1",
                1, locationsOfUser.size());

        scannerLocationDao.deleteScannerLocation(testLocation2.getName(), testUser1.getAugentID());
        locationsOfUser = scannerLocationDao.getLocationsToScanOfUser(testUser1.getAugentID());
        Assert.assertEquals("deleteScannerLocationTest, delete location1 of testUser1",
                0, locationsOfUser.size());
    }

    @Test
    public void deleteAllScannersOfLocationTest() throws SQLException {
        scannerLocationDao.deleteAllScannersOfLocation(testLocation1.getName());
        List<User> usersOfLocation = scannerLocationDao.getScannersOnLocation(testLocation1.getName());
        Assert.assertEquals("deleteAllScannersOfLocationTest, delete all from testLocation1",
                0, usersOfLocation.size());

        scannerLocationDao.deleteAllScannersOfLocation(testLocation2.getName());
        usersOfLocation = scannerLocationDao.getScannersOnLocation(testLocation2.getName());
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
                scannerLocationDao.isUserAllowedToScan(testUser1.getAugentID(), testLocation1.getName()));
        Assert.assertTrue("isUserAllowedToScanTest, testUser1 -> testLocation2",
                scannerLocationDao.isUserAllowedToScan(testUser1.getAugentID(), testLocation2.getName()));
        Assert.assertTrue("isUserAllowedToScanTest, testUser2 -> testLocation1",
                scannerLocationDao.isUserAllowedToScan(testUser2.getAugentID(), testLocation1.getName()));
    }
}
