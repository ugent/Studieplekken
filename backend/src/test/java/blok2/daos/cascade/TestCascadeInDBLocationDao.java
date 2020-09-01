package blok2.daos.cascade;

import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.helpers.Language;
import blok2.helpers.date.CustomDate;
import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.CalendarPeriodForLockers;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import blok2.model.reservations.LocationReservation;
import blok2.model.reservations.LockerReservation;
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
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestCascadeInDBLocationDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    private ICalendarPeriodForLockersDao calendarPeriodForLockersDao;

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

    @Autowired
    private IAuthorityDao authorityDao;

    // this will be the test user
    private Location testLocation;

    // for cascade on SCANNERS_LOCATION, LOCATION_RESERVATIONS,
    // LOCKER_RESERVATIONS and PENALTY_BOOK,
    // some Users need to be available
    private User testUser1;
    private User testUser2;

    //to connect a location to an authority
    private Authority authority;

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

    // to test cascade on CALENDAR_PERIODS
    private List<CalendarPeriod> testCalendarPeriods;

    // to test cascade on CALENDAR_PERIODS_FOR_LOCKERS
    private List<CalendarPeriodForLockers> testCalendarPeriodsForLockers;

    @Before
    public void setup() throws SQLException {
        // Use test database
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(calendarPeriodDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(calendarPeriodForLockersDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationReservationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(lockerReservationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(penaltyEventsDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(scannerLocationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(authorityDao);

        // Setup test objects
        authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.getAuthorityId());
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

        testCalendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);
        testCalendarPeriodsForLockers = TestSharedMethods.testCalendarPeriodsForLockers(testLocation);

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

        calendarPeriodDao.addCalendarPeriods(testCalendarPeriods);
        calendarPeriodForLockersDao.addCalendarPeriodsForLockers(testCalendarPeriodsForLockers);
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database
        // Note, I am not relying on the cascade because that's
        // what we are testing here in this class ...
        calendarPeriodForLockersDao.deleteCalendarPeriodsForLockers(testCalendarPeriodsForLockers);
        calendarPeriodDao.deleteCalendarPeriods(testCalendarPeriods);

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

        accountDao.deleteUser(testUser2.getAugentID());
        accountDao.deleteUser(testUser1.getAugentID());

        // ... okay, cascade is assumed to be okay for the lockers here... (but it is)
        locationDao.deleteLocation(testLocation.getName());
        authorityDao.deleteAuthority(authority.getAuthorityId());

        // Use regular database
        accountDao.useDefaultDatabaseConnection();
        calendarPeriodForLockersDao.useDefaultDatabaseConnection();
        calendarPeriodDao.useDefaultDatabaseConnection();
        locationDao.useDefaultDatabaseConnection();
        locationReservationDao.useDefaultDatabaseConnection();
        lockerReservationDao.useDefaultDatabaseConnection();
        penaltyEventsDao.useDefaultDatabaseConnection();
        scannerLocationDao.useDefaultDatabaseConnection();
        authorityDao.useDefaultDatabaseConnection();
    }

    @Test
    public void updateLocationWithoutCascadeNeededTest() throws SQLException {
        updateLocationWithoutChangeInFK(testLocation);

        // LOCATIONS updated?
        locationDao.updateLocation(testLocation.getName(), testLocation);
        Location location = locationDao.getLocation(testLocation.getName());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, location", testLocation, location);

        // LOCKERS still available?
        List<Locker> lockers = locationDao.getLockers(testLocation.getName());
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

        // CALENDAR_PERIODS still available?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        testCalendarPeriods.sort(Comparator.comparing(CalendarPeriod::toString));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar periods",
                testCalendarPeriods, actualPeriods);

        // CALENDAR_PERIODS_FOR_LOCKERS still available?
        List<CalendarPeriodForLockers> actualPeriodsForLockers = calendarPeriodForLockersDao.getCalendarPeriodsForLockersOfLocation(testLocation.getName());
        actualPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));
        testCalendarPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar periods for lockers",
                testCalendarPeriodsForLockers, actualPeriodsForLockers);
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
        List<Locker> lockers = locationDao.getLockers(testLocation.getName());
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

        // CALENDAR_PERIODS updated?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        testCalendarPeriods.sort(Comparator.comparing(CalendarPeriod::toString));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar periods",
                testCalendarPeriods, actualPeriods);

        // CALENDAR_PERIODS_FOR_LOCKERS still available?
        List<CalendarPeriodForLockers> actualPeriodsForLockers = calendarPeriodForLockersDao.getCalendarPeriodsForLockersOfLocation(testLocation.getName());
        actualPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));
        testCalendarPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar periods for lockers",
                testCalendarPeriodsForLockers, actualPeriodsForLockers);
    }

    @Test
    public void updateNumberOfLockersTest() throws SQLException {
        // from > to
        testLocation.setNumberOfLockers(testLocation.getNumberOfLockers() / 2);
        locationDao.updateLocation(testLocation.getName(), testLocation);

        List<Locker> lockers = locationDao.getLockers(testLocation.getName());
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

        List<Locker> lockers = locationDao.getLockers(testLocation.getName());
        Assert.assertEquals("deleteLocation, lockers", 0, lockers.size());

        List<CalendarPeriod> calendarPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        Assert.assertEquals("deleteLocation, calendar periods", 0, calendarPeriods.size());

        List<CalendarPeriodForLockers> calendarPeriodsForLockers = calendarPeriodForLockersDao.getCalendarPeriodsForLockersOfLocation(testLocation.getName());
        Assert.assertEquals("deleteLocation, calendar periods for lockers", 0, calendarPeriodsForLockers.size());

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
        location.setImageUrl("Changed URL");
    }
}
