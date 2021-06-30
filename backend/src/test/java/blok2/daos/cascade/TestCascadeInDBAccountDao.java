package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.helpers.Language;
import blok2.helpers.exceptions.NoSuchUserException;
import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.Building;
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

public class TestCascadeInDBAccountDao extends BaseTest {

    @Autowired
    private IUserDao userDao;

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
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired IBuildingDao buildingDao;

    // this will be the test user
    private User testUser;

    // to test cascade on LOCATION_RESERVATIONS
    private LocationReservation testLocationReservation1;
    private LocationReservation testLocationReservation2;

    private Penalty testPenalty1;
    private Penalty testPenalty2;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        testUser = TestSharedMethods.studentTestUser();

        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        // for cascade on LOCATION_RESERVATIONS, a Location must be available
        Location testLocation1 = locationDao.addLocation(TestSharedMethods.testLocation(authority.clone(), testBuilding));
        Location testLocation2 = locationDao.addLocation(TestSharedMethods.testLocation2(authority.clone(), testBuilding));

        CalendarPeriod cp1 = TestSharedMethods.testCalendarPeriods(testLocation1).get(0);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, cp1);
        CalendarPeriod cp2 = TestSharedMethods.testCalendarPeriods(testLocation2).get(0);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, cp2);

        testLocationReservation1 = new LocationReservation(testUser, cp1.getTimeslots().get(0),  null);
        testLocationReservation2 = new LocationReservation(testUser, cp2.getTimeslots().get(0),  null);

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH, "Dit is een test omschrijving van een penalty event met code 0");
        descriptions.put(Language.ENGLISH, "This is a test description of a penalty event with code 0");

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.
        PenaltyEvent testPenaltyEvent = new PenaltyEvent(0, 10, descriptions);
        testPenalty1 = new Penalty(testUser.getUserId(), testPenaltyEvent.getCode(), LocalDateTime.now(), LocalDate.now(), testLocation1, 10, "First test penalty");
        testPenalty2 = new Penalty(testUser.getUserId(), testPenaltyEvent.getCode(), LocalDateTime.of(1970, 1, 1, 0, 0, 0), LocalDate.now(), testLocation2, 20, "Second test penalty");

        // Add test objects to database
        userDao.addUser(testUser);

        locationReservationDao.addLocationReservation(testLocationReservation1);
        locationReservationDao.addLocationReservation(testLocationReservation2);

        penaltyEventsDao.addPenaltyEvent(testPenaltyEvent);
        penaltyDao.addPenalty(testPenalty1);
        penaltyDao.addPenalty(testPenalty2);
    }

    @Test
    public void updateUserTest() throws SQLException {
        updateUserFieldWithoutAUGentID(testUser);
        userDao.updateUser(testUser);
        User u = userDao.getUserById(testUser.getUserId());
        Assert.assertEquals("updateUserWithoutCascadeNeededTest", testUser, u);

        LocationReservation lr1 = locationReservationDao.getLocationReservation(
                testLocationReservation1.getUser().getUserId(),
                testLocationReservation1.getTimeslot());
        Assert.assertEquals("updateUserWithoutCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(
                testLocationReservation2.getUser().getUserId(),
                testLocationReservation2.getTimeslot());
        Assert.assertEquals("updateUserWithoutCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);

        List<Penalty> penalties = penaltyDao.getPenaltiesByUser(testUser.getUserId());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, penalties", expectedPenalties,
                penalties);
    }

    @Test
    public void deleteUserTest() throws SQLException {
        userDao.deleteUser(testUser.getUserId());
        try {
            userDao.getUserById(testUser.getUserId());
            Assert.fail("user must be deleted");
        } catch (NoSuchUserException e) {
            Assert.assertTrue(true);
        }

        List<Penalty> penalties = penaltyDao.getPenaltiesByUser(testUser.getUserId());
        Assert.assertEquals("deleteUserTest, penalties", 0, penalties.size());

        List<LocationReservation> locationReservations = locationReservationDao
                .getAllLocationReservationsOfUser(testUser.getUserId());
        Assert.assertEquals("deleteUserTest, location reservations", 0,
                locationReservations.size());
    }

    private void updateUserFieldWithoutAUGentID(User user) {
        user.setLastName("Changed last name");
        user.setFirstName("Changed first name");
        user.setMail("Changed.Mail@UGent.be");
        user.setPassword("Changed Password");
        user.setInstitution("UGent");
        user.setAdmin(false);
    }
}
