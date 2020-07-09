package be.ugent.blok2.daos;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.helpers.Institution;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestScannerLocation {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    private Location testLocation, testLocation2;

    private User scannerEmployee;
    private User scannerStudent;

    // combine IAccountDao with ILocationDao to test scanner location functionality
    @Before
    public void setup() {
        // Change database credentials for used daos
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);

        testLocation = TestSharedMethods.setupTestLocation();

        testLocation2 = new Location();
        testLocation2.setName("Second Test Location");
        testLocation2.setAddress("Second Test street, 20");
        testLocation2.setNumberOfSeats(100);
        testLocation2.setNumberOfLockers(10);
        testLocation2.setMapsFrame("Second Test Google Maps frame");
        testLocation2.getDescriptions().put(Language.DUTCH, "Dit is een tweede testlocatie.");
        testLocation2.getDescriptions().put(Language.ENGLISH, "This is a second test location.");
        testLocation2.setImageUrl("https://example.com/picture.png");

        // setup test user objects
        scannerEmployee = new User();
        scannerEmployee.setFirstName("Scanner1");
        scannerEmployee.setLastName("Employee");
        scannerEmployee.setMail("scanner1.employee@ugent.be");
        scannerEmployee.setInstitution(Institution.UGent);
        scannerEmployee.setAugentID("003");
        scannerEmployee.setRoles(new Role[]{Role.EMPLOYEE});

        scannerStudent = new User();
        scannerStudent.setFirstName("Scanner2");
        scannerStudent.setLastName("Student");
        scannerStudent.setMail("scanner2.student@ugent.be");
        scannerStudent.setInstitution(Institution.UGent);
        scannerStudent.setAugentID("004");
        scannerStudent.setRoles(new Role[]{Role.STUDENT, Role.EMPLOYEE});
    }

    @After
    public void cleanup() {
        accountDao.useDefaultDatabaseConnection();
    }

    @Test
    public void locationScannersTest() {
        // setup test scenario
        locationDao.addLocation(testLocation);
        locationDao.addLocation(testLocation2);

        addTestUsers();

        // add both test users as scanners for both test locations
        List<User> users = new ArrayList<>();
        users.add(scannerEmployee);
        users.add(scannerStudent);
        locationDao.setScannersForLocation(testLocation.getName(), users);
        locationDao.setScannersForLocation(testLocation2.getName(), users);

        // test whether fetching the locations of which the test users are scanners, is successful
        List<String> locationsOfEmployee = accountDao.getScannerLocations(scannerEmployee.getMail());
        List<String> locationsOfStudent = accountDao.getScannerLocations(scannerStudent.getMail());

        List<String> expectedLocations = new ArrayList<>();
        expectedLocations.add(testLocation.getName());
        expectedLocations.add(testLocation2.getName());

        // sort everything to make sure the order of equal contents are the same
        locationsOfEmployee.sort(String::compareTo);
        locationsOfStudent.sort(String::compareTo);
        expectedLocations.sort(String::compareTo);

        Assert.assertArrayEquals("locationScannersTest, correct locations fetched for employee?",
                expectedLocations.toArray(), locationsOfEmployee.toArray());
        Assert.assertArrayEquals("locationScannersTest, correct locations fetched for student?",
                expectedLocations.toArray(), locationsOfStudent.toArray());

        // test whether fetching the users which are scanners for the test locations, is successful
        List<String> usersForTestLocation = locationDao.getScannersFromLocation(testLocation.getName());
        List<String> usersForTestLocation2 = locationDao.getScannersFromLocation(testLocation2.getName());

        List<String> expectedUserAugentIds = new ArrayList<>();
        expectedUserAugentIds.add(scannerStudent.getAugentID());
        expectedUserAugentIds.add(scannerEmployee.getAugentID());

        // sort everything to make sure the order of equal contents are the same
        usersForTestLocation.sort(String::compareTo);
        usersForTestLocation2.sort(String::compareTo);
        expectedUserAugentIds.sort(String::compareTo);

        Assert.assertArrayEquals("locationScannersTest, correct users fetched for testLocation?",
                expectedUserAugentIds.toArray(), usersForTestLocation.toArray());
        Assert.assertArrayEquals("locationScannersTest, correct users fetched for testLocation2?",
                expectedUserAugentIds.toArray(), usersForTestLocation2.toArray());

        // rollback test setup
        removeTestUsers();

        locationDao.deleteLocation(testLocation2.getName());
        locationDao.deleteLocation(testLocation.getName());
    }

    private void addTestUsers() {
        accountDao.directlyAddUser(scannerEmployee);
        accountDao.directlyAddUser(scannerStudent);

        User u1 = accountDao.getUserById(scannerEmployee.getAugentID());
        Assert.assertEquals("Setup scannerEmployee failed", scannerEmployee, u1);

        User u2 = accountDao.getUserById(scannerStudent.getAugentID());
        Assert.assertEquals("Setup scannerStudent failed", scannerStudent, u2);
    }

    private void removeTestUsers() {
        accountDao.removeUserById(scannerEmployee.getAugentID());
        accountDao.removeUserById(scannerStudent.getAugentID());

        User u1 = accountDao.getUserById(scannerEmployee.getAugentID());
        Assert.assertNull("Cleanup scannerEmployee failed", u1);

        User u2 = accountDao.getUserById(scannerStudent.getAugentID());
        Assert.assertNull("Cleanup scannerStudent failed", u2);
    }
}
