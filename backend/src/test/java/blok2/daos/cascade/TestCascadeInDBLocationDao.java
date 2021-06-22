package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.helpers.Language;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TestCascadeInDBLocationDao extends BaseTest {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private ILocationReservationDao locationReservationDao;

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

    // for cascade on SCANNERS_LOCATION, LOCATION_RESERVATIONS
    // and PENALTY_BOOK, some Users need to be available
    private User testUser1;
    private User testUser2;

    // to test cascade on LOCATION_RESERVATIONS
    private LocationReservation testLocationReservation1;
    private LocationReservation testLocationReservation2;

    private Penalty testPenalty1;
    private Penalty testPenalty2;

    // to test cascade on CALENDAR_PERIODS
    private List<CalendarPeriod> testCalendarPeriods;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        testUser1 = TestSharedMethods.studentTestUser();
        testUser2 = TestSharedMethods.adminTestUser();
        locationDao.addLocation(testLocation);

        CalendarPeriod cp1 = TestSharedMethods.testCalendarPeriods(testLocation).get(0);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, cp1);
        CalendarPeriod cp2 = TestSharedMethods.testCalendarPeriods(testLocation).get(0);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, cp2);

        testCalendarPeriods = Arrays.asList(cp1, cp2);
        testLocationReservation1 = new LocationReservation(testUser1, LocalDateTime.now(), cp1.getTimeslots().get(0),  null);
        testLocationReservation2 = new LocationReservation(testUser2,  LocalDateTime.of(1970,1,1,0,0), cp2.getTimeslots().get(0),  null);

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH, "Dit is een test omschrijving van een penalty event met code 0");
        descriptions.put(Language.ENGLISH, "This is a test description of a penalty event with code 0");
        // to test cascade on PENALTY_BOOK
        PenaltyEvent testPenaltyEvent = new PenaltyEvent(0, 10, descriptions);

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.
        testPenalty1 = new Penalty(testUser1.getUserId(), testPenaltyEvent.getCode(), LocalDate.now(), LocalDate.now(), testLocation.getLocationId(), 10, "First test penalty");
        testPenalty2 = new Penalty(testUser2.getUserId(), testPenaltyEvent.getCode(), LocalDate.now(), LocalDate.now(), testLocation.getLocationId(), 20, "Second test penalty");

        // Add test objects to database
        accountDao.directlyAddUser(testUser1);
        accountDao.directlyAddUser(testUser2);

        locationReservationDao.addLocationReservation(testLocationReservation1);
        locationReservationDao.addLocationReservation(testLocationReservation2);

        penaltyEventsDao.addPenaltyEvent(testPenaltyEvent);
        penaltyEventsDao.addPenalty(testPenalty1);
        penaltyEventsDao.addPenalty(testPenalty2);

        scannerLocationDao.addScannerLocation(testLocation.getLocationId(), testUser1.getUserId());
        scannerLocationDao.addScannerLocation(testLocation.getLocationId(), testUser2.getUserId());
    }

    @Test
    public void updateLocationWithoutCascadeNeededTest() throws SQLException {
        updateLocationWithoutChangeInFK(testLocation);

        // LOCATIONS updated?
        locationDao.updateLocation(testLocation.getLocationId(), testLocation);
        Location location = locationDao.getLocationByName(testLocation.getName());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, location", testLocation, location);

        // LOCATION_RESERVATIONS still available?
        LocationReservation lr1 = locationReservationDao.getLocationReservation(
                testLocationReservation1.getUser().getUserId(),
                testLocationReservation1.getTimeslot());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(
                testLocationReservation2.getUser().getUserId(),
                testLocationReservation2.getTimeslot());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);

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
        scanners.sort(Comparator.comparing(User::getUserId));

        List<User> expectedScanners = new ArrayList<>();
        expectedScanners.add(testUser1);
        expectedScanners.add(testUser2);
        expectedScanners.sort(Comparator.comparing(User::getUserId));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, locations to scan with new id",
                expectedScanners, scanners);

        // CALENDAR_PERIODS still available?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getLocationId());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::getId));
        testCalendarPeriods.sort(Comparator.comparing(CalendarPeriod::getId));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar periods",
                testCalendarPeriods, actualPeriods);
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

        // CALENDAR_PERIODS updated?
        LocationReservation lr1 = locationReservationDao.getLocationReservation(testUser1.getUserId(),
                testLocationReservation1.getTimeslot());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(testUser2.getUserId(),
                testLocationReservation2.getTimeslot());
        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);

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
        scanners.sort(Comparator.comparing(User::getUserId));

        List<User> expectedScanners = new ArrayList<>();
        expectedScanners.add(testUser1);
        expectedScanners.add(testUser2);
        expectedScanners.sort(Comparator.comparing(User::getUserId));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, locations to scan with new id",
                expectedScanners, scanners);

        // CALENDAR_PERIODS updated?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getLocationId());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        testCalendarPeriods.sort(Comparator.comparing(CalendarPeriod::toString));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar periods",
                testCalendarPeriods, actualPeriods);
    }

    @Test
    public void deleteLocationTest() throws SQLException {
        locationDao.deleteLocation(testLocation.getLocationId());
        Location l = locationDao.getLocationByName(testLocation.getName());
        Assert.assertNull("deleteLocation, location must be deleted", l);

        List<CalendarPeriod> calendarPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteLocation, calendar periods", 0, calendarPeriods.size());

        List<User> scanners = scannerLocationDao.getScannersOnLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteLocation, scanners", 0, scanners.size());

        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteLocation, penalties", 0, penalties.size());
    }

    private void updateLocationWithoutChangeInFK(Location location) {
        location.setNumberOfLockers(100);
        location.setNumberOfSeats(200);
        location.setImageUrl("Changed URL");
    }
}
