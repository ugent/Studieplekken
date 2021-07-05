package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
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
    private IUserDao userDao;

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private ILocationReservationDao locationReservationDao;

    @Autowired
    private IPenaltyEventsDao penaltyEventsDao;

    @Autowired
    private IPenaltyDao penaltyDao;

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
        testLocationReservation1 = new LocationReservation(testUser1, cp1.getTimeslots().get(0),  null);
        testLocationReservation2 = new LocationReservation(testUser2, cp2.getTimeslots().get(0),  null);

        // to test cascade on PENALTY_BOOK
        PenaltyEvent testPenaltyEvent = penaltyEventsDao.addPenaltyEvent(new PenaltyEvent(null, 10,
                "Dit is een test omschrijving van een penalty event",
                "This is a test description of a penalty event"));

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.
        testPenalty1 = new Penalty(testUser1.getUserId(), testPenaltyEvent.getCode(), LocalDateTime.now(), LocalDate.now(), testLocation, 10, "First test penalty");
        testPenalty2 = new Penalty(testUser2.getUserId(), testPenaltyEvent.getCode(), LocalDateTime.now(), LocalDate.now(), testLocation, 20, "Second test penalty");

        // Add test objects to database
        userDao.addUser(testUser1);
        userDao.addUser(testUser2);

        locationReservationDao.addLocationReservation(testLocationReservation1);
        locationReservationDao.addLocationReservation(testLocationReservation2);

        penaltyDao.addPenalty(testPenalty1);
        penaltyDao.addPenalty(testPenalty2);
    }

    @Test
    public void updateLocationWithoutCascadeNeededTest() throws SQLException {
        updateLocationWithoutChangeInFK(testLocation);

        // LOCATIONS updated?
        locationDao.updateLocation(testLocation);
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
        List<Penalty> penalties = penaltyDao.getPenaltiesByLocation(testLocation.getLocationId());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, penalties", expectedPenalties,
                penalties);

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
        locationDao.updateLocation(testLocation);

        // old location should be deleted ...
        try {
            locationDao.getLocationByName(oldName);
        } catch (NoSuchDatabaseObjectException ignore) {
            Assert.assertTrue("Old location must be deleted and thus a NoSuchDatabaseObjectException should have been thrown.", true);
        }

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
        List<Penalty> penalties = penaltyDao.getPenaltiesByLocation(testLocation.getLocationId());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        // Penalty objects don't keep a reference to User, but have a String with the userId
        testPenalty1.setReservationLocation(testLocation);
        testPenalty2.setReservationLocation(testLocation);

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateLocationWithoutCascadeNeededTest, penalties", expectedPenalties,
                penalties);

        // CALENDAR_PERIODS updated?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getLocationId());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::getId));
        testCalendarPeriods.sort(Comparator.comparing(CalendarPeriod::getId));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, calendar periods",
                testCalendarPeriods, actualPeriods);
    }

    @Test
    public void deleteLocationTest() throws SQLException {
        locationDao.deleteLocation(testLocation.getLocationId());
        try {
            locationDao.getLocationByName(testLocation.getName());
        } catch (NoSuchDatabaseObjectException ignore) {
            Assert.assertTrue("Location must be deleted", true);
        }

        List<CalendarPeriod> calendarPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteLocation, calendar periods", 0, calendarPeriods.size());

        List<Penalty> penalties = penaltyDao.getPenaltiesByLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteLocation, penalties", 0, penalties.size());
    }

    private void updateLocationWithoutChangeInFK(Location location) {
        location.setNumberOfLockers(100);
        location.setNumberOfSeats(200);
        location.setImageUrl("Changed URL");
    }
}
