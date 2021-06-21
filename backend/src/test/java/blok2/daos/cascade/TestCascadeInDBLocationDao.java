package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.helpers.Language;
import blok2.helpers.Pair;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.CalendarPeriodForLockers;
import blok2.model.calendar.Timeslot;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import blok2.model.reservations.LocationReservation;
import blok2.model.reservations.LockerReservation;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TestCascadeInDBLocationDao extends BaseTest {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ITimeslotDAO timeslotDAO;

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

    @Autowired
    private IBuildingDao buildingDao;

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

    private Penalty testPenalty1;
    private Penalty testPenalty2;

    // to test cascade on CALENDAR_PERIODS_FOR_LOCKERS
    private List<CalendarPeriodForLockers> testCalendarPeriodsForLockers;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        testUser1 = TestSharedMethods.studentTestUser();
        testUser2 = TestSharedMethods.adminTestUser();
        locationDao.addLocation(testLocation);

        List<Timeslot> cp1 = TestSharedMethods.addPair(timeslotDAO, TestSharedMethods.testCalendarPeriods(testLocation).get(0));
        List<Timeslot> cp2 = TestSharedMethods.addPair(timeslotDAO, TestSharedMethods.testCalendarPeriods(testLocation).get(0));

        testLocationReservation1 = new LocationReservation(testUser1, LocalDateTime.now(), cp1.get(0),  null);
        testLocationReservation2 = new LocationReservation(testUser2,  LocalDateTime.of(1970,1,1,0,0), cp2.get(0),  null);

        Locker testLocker1 = new Locker(0, testLocation);
        Locker testLocker2 = new Locker(1, testLocation);
        testLockerReservation1 = new LockerReservation(testLocker1, testUser1);
        testLockerReservation2 = new LockerReservation(testLocker2, testUser2);

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH, "Dit is een test omschrijving van een penalty event met code 0");
        descriptions.put(Language.ENGLISH, "This is a test description of a penalty event with code 0");
        // to test cascade on PENALTY_BOOK
        PenaltyEvent testPenaltyEvent = new PenaltyEvent(0, 10, descriptions);

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.
        testPenalty1 = new Penalty(testUser1.getAugentID(), testPenaltyEvent.getCode(), LocalDate.now(), LocalDate.now(), testLocation.getLocationId(), 10, "First test penalty");
        testPenalty2 = new Penalty(testUser2.getAugentID(), testPenaltyEvent.getCode(), LocalDate.now(), LocalDate.now(), testLocation.getLocationId(), 20, "Second test penalty");

        testCalendarPeriodsForLockers = TestSharedMethods.testCalendarPeriodsForLockers(testLocation);

        // Add test objects to database
        accountDao.directlyAddUser(testUser1);
        accountDao.directlyAddUser(testUser2);

        locationReservationDao.addLocationReservation(testLocationReservation1);
        locationReservationDao.addLocationReservation(testLocationReservation2);

        lockerReservationDao.addLockerReservation(testLockerReservation1);
        lockerReservationDao.addLockerReservation(testLockerReservation2);

        penaltyEventsDao.addPenaltyEvent(testPenaltyEvent);
        penaltyEventsDao.addPenalty(testPenalty1);
        penaltyEventsDao.addPenalty(testPenalty2);

        scannerLocationDao.addScannerLocation(testLocation.getLocationId(), testUser1.getAugentID());
        scannerLocationDao.addScannerLocation(testLocation.getLocationId(), testUser2.getAugentID());
    }

    @Test
    public void updateLocationWithoutCascadeNeededTest() throws SQLException {
        updateLocationWithoutChangeInFK(testLocation);

        // LOCATIONS updated?
        locationDao.updateLocation(testLocation.getLocationId(), testLocation);
        Location location = locationDao.getLocationByName(testLocation.getName());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, location", testLocation, location);

        // LOCKERS still available?
        List<Locker> lockers = locationDao.getLockers(testLocation.getLocationId());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, lockers",
                testLocation.getNumberOfLockers(), lockers.size());

        // LOCATION_RESERVATIONS still available?
        LocationReservation lr1 = locationReservationDao.getLocationReservation(
                testLocationReservation1.getUser().getAugentID(),
                testLocationReservation1.getTimeslot());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(
                testLocationReservation2.getUser().getAugentID(),
                testLocationReservation2.getTimeslot());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);

/*
        // LOCKER_RESERVATIONS still available?
        LockerReservation lor1 = lockerReservationDao.getLockerReservation(
                testLockerReservation1.getLocker().getLocationByName().getName(),
                testLockerReservation1.getLocker().getNumber());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLockerReservation1",
                testLockerReservation1, lor1);

        LockerReservation lor2 = lockerReservationDao.getLockerReservation(
                testLockerReservation2.getLocker().getLocationByName().getName(),
                testLockerReservation2.getLocker().getNumber());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLockerReservation2",
                testLockerReservation2, lor2);
*/
        // PENALTY_BOOK entries still available?
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByLocation(testLocation.getLocationId());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, penalties", expectedPenalties,
                penalties);

        // SCANNERS_LOCATION entries still available?
        List<User> scanners = scannerLocationDao.getScannersOnLocation(testLocation.getLocationId());
        scanners.sort(Comparator.comparing(User::getAugentID));

        List<User> expectedScanners = new ArrayList<>();
        expectedScanners.add(testUser1);
        expectedScanners.add(testUser2);
        expectedScanners.sort(Comparator.comparing(User::getAugentID));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, locations to scan with new id",
                expectedScanners, scanners);

        // CALENDAR_PERIODS_FOR_LOCKERS still available?
        /*
        List<CalendarPeriodForLockers> actualPeriodsForLockers = calendarPeriodForLockersDao.getCalendarPeriodsForLockersOfLocation(testLocation.getName());
        actualPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers:));
        testCalendarPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar periods for lockers",
                testCalendarPeriodsForLockers, actualPeriodsForLockers); */
    }

    @Test
    public void updateLocationWithCascadeNeededTest() throws SQLException {
        updateLocationWithoutChangeInFK(testLocation);
        String oldName = testLocation.getName();
        testLocation.setName("Changed name of location");
        locationDao.updateLocation(testLocation.getLocationId(), testLocation);

        // old location should be deleted ...
        Location old = locationDao.getLocationByName(oldName);
        Assert.assertNull("updateLocationWithCascadeNeededTest, old location must be deleted", old);

        // ... and should be available under its new name
        Location location = locationDao.getLocationByName(testLocation.getName());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, location", testLocation, location);

        // LOCKERS updated? (see updateNumberOfLockersTest() for extensive LOCKERS test)
        List<Locker> lockers = locationDao.getLockers(testLocation.getLocationId());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, lockers",
                testLocation.getNumberOfLockers(), lockers.size());

        // CALENDAR_PERIODS updated?
        LocationReservation lr1 = locationReservationDao.getLocationReservation(testUser1.getAugentID(),
                testLocationReservation1.getTimeslot());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(testUser2.getAugentID(),
                testLocationReservation2.getTimeslot());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);
/*
        // LOCKER_RESERVATIONS updated?
        LockerReservation lor1 = lockerReservationDao.getLockerReservation(
                testLockerReservation1.getLocker().getLocationByName().getName(),
                testLockerReservation1.getLocker().getNumber());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLockerReservation1",
                testLockerReservation1, lor1);

        LockerReservation lor2 = lockerReservationDao.getLockerReservation(
                testLockerReservation2.getLocker().getLocationByName().getName(),
                testLockerReservation2.getLocker().getNumber());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLockerReservation2",
                testLockerReservation2, lor2);
*/
        // PENALTY_BOOK updated?
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByLocation(testLocation.getLocationId());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        // Penalty objects don't keep a reference to User, but have a String with the augentid
        testPenalty1.setReservationLocationId(testLocation.getLocationId());
        testPenalty2.setReservationLocationId(testLocation.getLocationId());

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, penalties", expectedPenalties,
                penalties);

        // SCANNERS_LOCATION updated?
        List<User> scanners = scannerLocationDao.getScannersOnLocation(testLocation.getLocationId());
        scanners.sort(Comparator.comparing(User::getAugentID));

        List<User> expectedScanners = new ArrayList<>();
        expectedScanners.add(testUser1);
        expectedScanners.add(testUser2);
        expectedScanners.sort(Comparator.comparing(User::getAugentID));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, locations to scan with new id",
                expectedScanners, scanners);

    }

    @Test
    public void updateNumberOfLockersTest() throws SQLException {
        // from > to
        testLocation.setNumberOfLockers(testLocation.getNumberOfLockers() / 2);
        locationDao.updateLocation(testLocation.getLocationId(), testLocation);

        List<Locker> lockers = locationDao.getLockers(testLocation.getLocationId());
        Assert.assertEquals("updateNumberOfLockersTest, from > to", testLocation.getNumberOfLockers(),
                lockers.size());

        // from < to
        testLocation.setNumberOfLockers(testLocation.getNumberOfLockers() * 4);
        locationDao.updateLocation(testLocation.getLocationId(), testLocation);

        lockers = locationDao.getLockers(testLocation.getLocationId());
        Assert.assertEquals("updateNumberOfLockersTest, from < to", testLocation.getNumberOfLockers(),
                lockers.size());

        // set to 0
        testLocation.setNumberOfLockers(0);
        locationDao.updateLocation(testLocation.getLocationId(), testLocation);

        lockers = locationDao.getLockers(testLocation.getLocationId());
        Assert.assertEquals("updateNumberOfLockersTest, from < to", testLocation.getNumberOfLockers(),
                lockers.size());
    }

    @Test
    public void deleteLocationTest() throws SQLException {
        locationDao.deleteLocation(testLocation.getLocationId());
        Location l = locationDao.getLocationByName(testLocation.getName());
        Assert.assertNull("deleteLocation, location must be deleted", l);

        List<Locker> lockers = locationDao.getLockers(testLocation.getLocationId());
        Assert.assertEquals("deleteLocation, lockers", 0, lockers.size());

        List<User> scanners = scannerLocationDao.getScannersOnLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteLocation, scanners", 0, scanners.size());

        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteLocation, penalties", 0, penalties.size());
        
        //List<LockerReservation> lockerReservations = lockerReservationDao
         //       .getAllLockerReservationsOfLocation(testLocation.getName(), true);
       // Assert.assertEquals("deleteLocation, locker reservations", 0, lockerReservations.size());
    }

    private void updateLocationWithoutChangeInFK(Location location) {
        location.setNumberOfLockers(100);
        location.setNumberOfSeats(200);
        location.setImageUrl("Changed URL");
    }
}
