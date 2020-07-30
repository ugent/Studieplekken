package be.ugent.blok2.daos.cascade;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.daos.*;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservables.Locker;
import be.ugent.blok2.model.reservations.LocationReservation;
import be.ugent.blok2.model.reservations.LockerReservation;
import be.ugent.blok2.model.users.User;
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
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestCascadeInDBLocationDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private ILocationReservationDao locationReservationDao;

    @Autowired
    private ILockerReservationDao lockerReservationDao;

    @Autowired
    private IPenaltyEventsDao penaltyEventsDao;

    @Autowired
    private IScannerLocationDao scannerLocationDao;

    // this will be the test user
    private Location testLocation;

    // for cascade on SCANNERS_LOCATION, LOCATION_RESERVATIONS,
    // LOCKER_RESERVATIONS and PENALTY_BOOK,
    // some Users need to be available
    private User testUser1;
    private User testUser2;

    // to test cascade on LOCATION_RESERVATIONS
    private LocationReservation testLocationReservation1;
    private LocationReservation testLocationReservation2;

    // to test cascade on LOCKER_RESERVATIONS
    private LockerReservation testLockerReservation1;
    private LockerReservation testLockerReservation2;

    // to test cascade on PENALTY_BOOK
    private PenaltyEvent testPenaltyEvent;
    private Penalty testPenalty1;
    private Penalty testPenalty2;

    // to test cascade on CALENDAR
    private List<Day> testCalendarDays;

    @Before
    public void setup() throws SQLException {
        // Use test database
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationReservationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(lockerReservationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(penaltyEventsDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(scannerLocationDao);

        // Setup test objects
        testLocation = TestSharedMethods.testLocation();
        testUser1 = TestSharedMethods.studentEmployeeTestUser();
        testUser2 = TestSharedMethods.employeeAdminTestUser();

        testLocationReservation1 = new LocationReservation(testLocation, testUser1, CustomDate.now());
        testLocationReservation2 = new LocationReservation(testLocation, testUser2, CustomDate.now());

        Locker testLocker1 = new Locker(0, testLocation);
        Locker testLocker2 = new Locker(1, testLocation);
        testLockerReservation1 = new LockerReservation(testLocker1, testUser1);
        testLockerReservation2 = new LockerReservation(testLocker2, testUser2);

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH, "Dit is een test omschrijving van een penalty event met code 0");
        descriptions.put(Language.ENGLISH, "This is a test description of a penalty event with code 0");
        testPenaltyEvent = new PenaltyEvent(0, 10, true, descriptions);

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.
        testPenalty1 = new Penalty(testUser1.getAugentID(), testPenaltyEvent.getCode(), CustomDate.now(), CustomDate.now(), testLocation.getName(), 10);
        testPenalty2 = new Penalty(testUser2.getAugentID(), testPenaltyEvent.getCode(), CustomDate.now(), CustomDate.now(), testLocation.getName(), 20);

        // to test cascade on CALENDAR
        Calendar testCalendar = new Calendar();
        CustomDate date1 = new CustomDate(1970, 1, 1);
        CustomDate date2 = new CustomDate(1970, 1, 2);
        Time open = new Time(9, 0, 0);
        Time close = new Time(17, 0, 0);
        Day day1 = new Day(date1, open, close, date1);
        Day day2 = new Day(date2, open, close, date1);

        testCalendar.setDays(Arrays.asList(day1, day2));
        testCalendarDays = new ArrayList<>(Arrays.asList(day1, day2));

        // Add test objects to database
        locationDao.addLocation(testLocation);
        accountDao.directlyAddUser(testUser1);
        accountDao.directlyAddUser(testUser2);

        locationReservationDao.addLocationReservation(testLocationReservation1);
        locationReservationDao.addLocationReservation(testLocationReservation2);

        lockerReservationDao.addLockerReservation(testLockerReservation1);
        lockerReservationDao.addLockerReservation(testLockerReservation2);

        penaltyEventsDao.addPenaltyEvent(testPenaltyEvent);
        penaltyEventsDao.addPenalty(testPenalty1);
        penaltyEventsDao.addPenalty(testPenalty2);

        scannerLocationDao.addScannerLocation(testLocation.getName(), testUser1.getAugentID());
        scannerLocationDao.addScannerLocation(testLocation.getName(), testUser2.getAugentID());

        locationDao.addCalendarDays(testLocation.getName(), testCalendar);
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database
        locationDao.deleteCalendarDays(testLocation.getName(), "1950-01-01T00:00:00",
                CustomDate.now().toString());
        scannerLocationDao.deleteAllScannersOfLocation(testLocation.getName());
        penaltyEventsDao.deletePenalty(testPenalty2);
        penaltyEventsDao.deletePenalty(testPenalty1);
        penaltyEventsDao.deletePenaltyEvent(testPenaltyEvent.getCode());
        lockerReservationDao.deleteLockerReservation(testLockerReservation2.getLocker().getLocation().getName(),
                testLockerReservation2.getLocker().getNumber());
        lockerReservationDao.deleteLockerReservation(testLockerReservation1.getLocker().getLocation().getName(),
                testLockerReservation1.getLocker().getNumber());
        locationReservationDao.deleteLocationReservation(testLocationReservation2.getUser().getAugentID(),
                testLocationReservation2.getDate());
        locationReservationDao.deleteLocationReservation(testLocationReservation1.getUser().getAugentID(),
                testLocationReservation1.getDate());
        // ... okay, cascade is assumed to be okay for the lockers here... (but it is)
        accountDao.deleteUser(testUser2.getAugentID());
        accountDao.deleteUser(testUser1.getAugentID());
        locationDao.deleteLocation(testLocation.getName());

        // Use regular database
        accountDao.useDefaultDatabaseConnection();
        locationDao.useDefaultDatabaseConnection();
        locationReservationDao.useDefaultDatabaseConnection();
        lockerReservationDao.useDefaultDatabaseConnection();
        penaltyEventsDao.useDefaultDatabaseConnection();
        scannerLocationDao.useDefaultDatabaseConnection();
    }

    @Test
    public void updateLocationWithoutCascadeNeededTest() throws SQLException {
        updateLocationWithoutChangeInFK(testLocation);

        // LOCATIONS and LOCATION_DESCRIPTIONS updated?
        locationDao.updateLocation(testLocation.getName(), testLocation);
        Location location = locationDao.getLocation(testLocation.getName());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, location", testLocation, location);

        // LOCKERS still available?
        Collection<Locker> lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, lockers",
                testLocation.getNumberOfLockers(), lockers.size());

        // LOCATION_RESERVATIONS still available?
        LocationReservation lr1 = locationReservationDao.getLocationReservation(
                testLocationReservation1.getUser().getAugentID(),
                testLocationReservation1.getDate());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(
                testLocationReservation2.getUser().getAugentID(),
                testLocationReservation2.getDate());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);

        // LOCKER_RESERVATIONS still available?
        LockerReservation lor1 = lockerReservationDao.getLockerReservation(
                testLockerReservation1.getLocker().getLocation().getName(),
                testLockerReservation1.getLocker().getNumber());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLockerReservation1",
                testLockerReservation1, lor1);

        LockerReservation lor2 = lockerReservationDao.getLockerReservation(
                testLockerReservation2.getLocker().getLocation().getName(),
                testLockerReservation2.getLocker().getNumber());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLockerReservation2",
                testLockerReservation2, lor2);

        // PENALTY_BOOK entries still available?
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByLocation(testLocation.getName());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, penalties", expectedPenalties,
                penalties);

        // SCANNERS_LOCATION entries still available?
        List<User> scanners = scannerLocationDao.getScannersOnLocation(testLocation.getName());
        scanners.sort(Comparator.comparing(User::getAugentID));

        List<User> expectedScanners = new ArrayList<>();
        expectedScanners.add(testUser1);
        expectedScanners.add(testUser2);
        expectedScanners.sort(Comparator.comparing(User::getAugentID));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, locations to scan with new id",
                expectedScanners, scanners);

        // CALENDAR entries still available?
        Collection<Day> _calendarDays = locationDao.getCalendarDays(testLocation.getName());
        List<Day> calendarDays = new ArrayList<>(_calendarDays);

        calendarDays.sort(Comparator.comparing(a -> a.getDate().toString()));
        testCalendarDays.sort(Comparator.comparing(a -> a.getDate().toString()));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar days",
                testCalendarDays, calendarDays);
    }

    @Test
    public void updateLocationWithCascadeNeededTest() throws SQLException {
        updateLocationWithoutChangeInFK(testLocation);
        String oldName = testLocation.getName();
        testLocation.setName("Changed name of location");
        locationDao.updateLocation(oldName, testLocation);

        // old location should be deleted ...
        Location old = locationDao.getLocation(oldName);
        Assert.assertNull("updateLocationWithCascadeNeededTest, old location must be deleted", old);

        // ... and should be available under its new name
        Location location = locationDao.getLocation(testLocation.getName());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, location", testLocation, location);

        // LOCKERS updated? (see updateNumberOfLockersTest() for extensive LOCKERS test)
        Collection<Locker> lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, lockers",
                testLocation.getNumberOfLockers(), lockers.size());

        // LOCATION_RESERVATIONS updated?
        LocationReservation lr1 = locationReservationDao.getLocationReservation(
                testLocationReservation1.getUser().getAugentID(),
                testLocationReservation1.getDate());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(
                testLocationReservation2.getUser().getAugentID(),
                testLocationReservation2.getDate());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);

        // LOCKER_RESERVATIONS updated?
        LockerReservation lor1 = lockerReservationDao.getLockerReservation(
                testLockerReservation1.getLocker().getLocation().getName(),
                testLockerReservation1.getLocker().getNumber());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLockerReservation1",
                testLockerReservation1, lor1);

        LockerReservation lor2 = lockerReservationDao.getLockerReservation(
                testLockerReservation2.getLocker().getLocation().getName(),
                testLockerReservation2.getLocker().getNumber());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLockerReservation2",
                testLockerReservation2, lor2);

        // PENALTY_BOOK updated?
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByLocation(testLocation.getName());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        // Penalty objects don't keep a reference to User, but have a String with the augentid
        testPenalty1.setReservationLocation(testLocation.getName());
        testPenalty2.setReservationLocation(testLocation.getName());

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, penalties", expectedPenalties,
                penalties);

        // SCANNERS_LOCATION updated?
        List<User> scanners = scannerLocationDao.getScannersOnLocation(testLocation.getName());
        scanners.sort(Comparator.comparing(User::getAugentID));

        List<User> expectedScanners = new ArrayList<>();
        expectedScanners.add(testUser1);
        expectedScanners.add(testUser2);
        expectedScanners.sort(Comparator.comparing(User::getAugentID));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, locations to scan with new id",
                expectedScanners, scanners);

        // CALENDAR shouldn't have been updated, but do the test anyway
        Collection<Day> _calendarDays = locationDao.getCalendarDays(testLocation.getName());
        List<Day> calendarDays = new ArrayList<>(_calendarDays);

        calendarDays.sort(Comparator.comparing(a -> a.getDate().toString()));
        testCalendarDays.sort(Comparator.comparing(a -> a.getDate().toString()));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar days",
                testCalendarDays, calendarDays);
    }

    @Test
    public void updateNumberOfLockersTest() throws SQLException {
        // from > to
        testLocation.setNumberOfLockers(testLocation.getNumberOfLockers() / 2);
        locationDao.updateLocation(testLocation.getName(), testLocation);

        Collection<Locker> lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("updateNumberOfLockersTest, from > to", testLocation.getNumberOfLockers(),
                lockers.size());

        // from < to
        testLocation.setNumberOfLockers(testLocation.getNumberOfLockers() * 4);
        locationDao.updateLocation(testLocation.getName(), testLocation);

        lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("updateNumberOfLockersTest, from < to", testLocation.getNumberOfLockers(),
                lockers.size());

        // set to 0
        testLocation.setNumberOfLockers(0);
        locationDao.updateLocation(testLocation.getName(), testLocation);

        lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("updateNumberOfLockersTest, from < to", testLocation.getNumberOfLockers(),
                lockers.size());
    }

    @Test
    public void deleteLocationTest() throws SQLException {
        locationDao.deleteLocation(testLocation.getName());
        Location l = locationDao.getLocation(testLocation.getName());
        Assert.assertNull("deleteLocation, location must be deleted", l);

        Collection<Locker> lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("deleteLocation, lockers", 0, lockers.size());

        Collection<Day> calendarDays = locationDao.getCalendarDays(testLocation.getName());
        Assert.assertEquals("deleteLocation, calendar days", 0, calendarDays.size());

        List<User> scanners = scannerLocationDao.getScannersOnLocation(testLocation.getName());
        Assert.assertEquals("deleteLocation, scanners", 0, scanners.size());

        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByLocation(testLocation.getName());
        Assert.assertEquals("deleteLocation, penalties", 0, penalties.size());

        List<LocationReservation> locationReservations = locationReservationDao
                .getAllLocationReservationsOfLocation(testLocation.getName());
        Assert.assertEquals("deleteLocation, location reservations", 0, locationReservations.size());

        List<LockerReservation> lockerReservations = lockerReservationDao
                .getAllLockerReservationsOfLocation(testLocation.getName());
        Assert.assertEquals("deleteLocation, locker reservations", 0, lockerReservations.size());
    }

    private void updateLocationWithoutChangeInFK(Location location) {
        location.setAddress("Changed Address");
        location.setNumberOfLockers(100);
        location.setNumberOfSeats(200);
        location.setMapsFrame("Changed frame");

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.ENGLISH, "This is a changed descriptions for the location");
        descriptions.put(Language.DUTCH, "Dit is een aangepaste omschrijving van de locatie");
        location.setDescriptions(descriptions);

        location.setImageUrl("Changed URL");
        location.setStartPeriodLockers(new CustomDate(1970, 1, 1));
        location.setEndPeriodLockers(CustomDate.now());
    }
}
