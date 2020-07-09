package be.ugent.blok2.daos;

import be.ugent.blok2.helpers.Institution;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
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
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestDBLocationDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    private final ResourceBundle applicationProperties = Resources.applicationProperties;

    private Location testLocation, testLocation2;

    private User scannerEmployee;
    private User scannerStudent;

    @Before
    public void setup() {
        // Change database credentials for used daos
        locationDao.setDatabaseConnectionUrl(applicationProperties.getString("test_db_url"));
        locationDao.setDatabaseCredentials(
                applicationProperties.getString("test_db_user"),
                applicationProperties.getString("test_db_password")
        );

        accountDao.setDatabaseConnectionUrl(applicationProperties.getString("test_db_url"));
        accountDao.setDatabaseCredentials(
                applicationProperties.getString("test_db_user"),
                applicationProperties.getString("test_db_password")
        );

        // setup test location objects
        CustomDate startPeriodLockers = new CustomDate(1970, 1, 1, 9, 0, 0);
        CustomDate endPeriodLockers = new CustomDate(1970, 1, 31, 17, 0, 0);

        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("Test street, 10");
        testLocation.setNumberOfSeats(50);
        testLocation.setNumberOfLockers(15);
        testLocation.setMapsFrame("Test Google Maps frame");
        testLocation.getDescriptions().put(Language.DUTCH, "Dit is een testlocatie.");
        testLocation.getDescriptions().put(Language.ENGLISH, "This is a test location.");
        testLocation.setImageUrl("https://example.com/image.jpg");
        testLocation.setStartPeriodLockers(startPeriodLockers);
        testLocation.setEndPeriodLockers(endPeriodLockers);

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
        locationDao.useDefaultDatabaseConnection();
    }

    @Test
    public void addLocationTest() {
        locationDao.addLocation(testLocation);
        Location l = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertEquals("addLocation", testLocation, l);

        locationDao.deleteLocation(testLocation.getName());
        l = locationDao.getLocation(testLocation.getName());
        Assert.assertNull("addLocation, remove added test location", l);
    }

    @Test
    public void changeLocationTest() {
        locationDao.addLocation(testLocation);

        Location changedTestLocation = testLocation.clone();
        changedTestLocation.setName("Changed Test Location");

        locationDao.changeLocation(testLocation.getName(), changedTestLocation);
        Location location = locationDao.getLocationWithoutLockersAndCalendar(changedTestLocation.getName());
        Assert.assertEquals("changeLocationTest, fetch location by changed name", changedTestLocation, location);

        location = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertNull("changeLocationTest, old location name may not have an entry", location);

        locationDao.deleteLocation(changedTestLocation.getName());
    }

    @Test
    public void addLockersTest() {
        locationDao.addLocation(testLocation);
        Location expectedLocation = testLocation.clone();
        int prev_n = expectedLocation.getNumberOfLockers();

        // test adding positive amount of lockers
        int n = 10;
        expectedLocation.setNumberOfLockers(prev_n + n);
        locationDao.addLockers(testLocation.getName(), n);
        Location location = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertEquals("addLockersTest, added lockers", expectedLocation, location);

        // test adding negative amount of lockers
        int _n = -5;
        expectedLocation.setNumberOfLockers(prev_n + n + _n);
        locationDao.addLockers(testLocation.getName(), _n);
        location = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertEquals("addLocker, added negative amount of lockers", expectedLocation, location);

        // TODO: reserve lockers and expect SQLException

        locationDao.deleteLocation(testLocation.getName());
    }

    @Test
    public void deleteLockersTest() {
        locationDao.addLocation(testLocation);
        Location expectedLocation = testLocation.clone();
        int prev_n = expectedLocation.getNumberOfLockers();

        int n = 5;
        expectedLocation.setNumberOfLockers(prev_n - n);
        locationDao.deleteLockers(testLocation.getName(), prev_n - n);
        Location location = locationDao.getLocationWithoutLockersAndCalendar(testLocation.getName());
        Assert.assertEquals("deleteLockersTest", expectedLocation, location);

        locationDao.deleteLocation(testLocation.getName());
    }

    /*
    * getCalendarDays(), addCalendarDays() and deleteCalendarDays will be tested
    * */
    @Test
    public void calendarDaysTest() {
        locationDao.addLocation(testLocation);

        Calendar calendar = new Calendar();
        Collection<Day> calendarDays = calendar.getDays();
        for (int i = 1; i <= 5; i++) {
            Day d = new Day();
            d.setDate(CustomDate.parseString("2020-01-0" + i + "T00:00:00"));
            d.setOpeningHour(new Time(9, 0, 0));
            d.setClosingHour(new Time(17, 0, 0));
            d.setOpenForReservationDate(CustomDate.parseString("2019-12-31T09:00:00"));
            calendarDays.add(d);
        }

        locationDao.addCalendarDays(testLocation.getName(), calendar);

        Collection<Day> retrievedCalendarDays = locationDao.getCalendarDays(testLocation.getName());
        Assert.assertArrayEquals("calendarDaysTest, retrieved calendar days", calendarDays.toArray(), retrievedCalendarDays.toArray());

        locationDao.deleteCalendarDays(testLocation.getName(), "2020-01-01T00:00:00", "2020-01-05T00:00:00");
        retrievedCalendarDays = locationDao.getCalendarDays(testLocation.getName());
        Assert.assertArrayEquals("calendarDaysTest, deleted calendar days", new Day[]{}, retrievedCalendarDays.toArray());

        locationDao.deleteLocation(testLocation.getName());
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
