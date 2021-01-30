package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.helpers.Pair;
import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.Building;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestDBLocationReservationDao extends BaseTest {

    @Autowired
    private ILocationReservationDao locationReservationDao;

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    private IBuildingDao buildingDao;

    private Location testLocation;
    private Location testLocation2;
    private User testUser;
    private User testUser2;
    private List<CalendarPeriod> calendarPeriods;
    private CalendarPeriod calendarPeriod1Seat;
    private List<CalendarPeriod> calendarPeriodsForLocation2;

    @Override
    public void populateDatabase() throws SQLException {
        // setup test location objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        Location testLocation1Seat = TestSharedMethods.testLocation1Seat(authority.clone(), testBuilding);
        testLocation2 = TestSharedMethods.testLocation2(authority.clone(), testBuilding);

        testUser = TestSharedMethods.adminTestUser();
        testUser2 = TestSharedMethods.studentTestUser();
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);
        calendarPeriod1Seat = TestSharedMethods.testCalendarPeriods(testLocation1Seat).get(0);
        calendarPeriodsForLocation2 = TestSharedMethods.testCalendarPeriods(testLocation2);

        // Add test objects to database
        TestSharedMethods.addTestUsers(accountDao, testUser, testUser2);
        locationDao.addLocation(testLocation);
        locationDao.addLocation(testLocation1Seat);
        locationDao.addLocation(testLocation2);

        CalendarPeriod[] cps = new CalendarPeriod[calendarPeriods.size()];
        cps = calendarPeriods.toArray(cps);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, cps);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, calendarPeriod1Seat);
        cps = calendarPeriodsForLocation2.toArray(cps);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, cps);
    }

    @Test
    public void addLocationReservationTest() throws SQLException {
        // retrieve entries from database instead of using the added instances
        Location location = locationDao.getLocation(testLocation.getName());
        User u = accountDao.getUserById(testUser.getAugentID());
        Timeslot timeslot = calendarPeriods.get(0).getTimeslots().get(0);
        // check whether all retrieved instances equal to the added instances
        Assert.assertEquals("addLocationReservation, setup testLocation", testLocation, location);
        Assert.assertEquals("addLocationReservation, setup testUser", testUser, u);

        // Create LocationReservation
        LocationReservation lr = new LocationReservation(u, LocalDateTime.of(1970, 1, 1, 9, 0, 0), timeslot, null);

        // add LocationReservation to database
        locationReservationDao.addLocationReservationIfStillRoomAtomically(lr);

        // test whether LocationReservation has been added successfully
        LocationReservation rlr = locationReservationDao.getLocationReservation(u.getAugentID(), timeslot); // rlr = retrieved location reservation
        Assert.assertEquals("addLocationReservation, getLocationReservation", lr, rlr);

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfUser(u.getAugentID());
        Assert.assertEquals("addLocationReservation, getAllLocationReservationsOfUser", 1, list.size());

        // delete LocationReservation from database
        locationReservationDao.deleteLocationReservation(u.getAugentID(), timeslot);
        rlr = locationReservationDao.getLocationReservation(u.getAugentID(), timeslot);
        Assert.assertNull("addLocationReservationTest, delete LocationReservation", rlr);
    }

    @Test
    public void addLocationReservationButFullTest() throws SQLException {
        // retrieve entries from database instead of using the added instances
        User u = accountDao.getUserById(testUser.getAugentID());
        User u2 = accountDao.getUserById(testUser2.getAugentID());

        Timeslot timeslot = calendarPeriod1Seat.getTimeslots().get(0);

        LocationReservation lr = new LocationReservation(u, LocalDateTime.now(), timeslot, null);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, calendarPeriods.get(0));
        Assert.assertTrue(locationReservationDao.addLocationReservationIfStillRoomAtomically(lr));
        lr = new LocationReservation(u, LocalDateTime.now(), timeslot, null);
        // This is a duplicate entry into the database. Shouldn't work.
        Assert.assertFalse(locationReservationDao.addLocationReservationIfStillRoomAtomically(lr));
        lr = new LocationReservation(u2, LocalDateTime.now(), timeslot, null);
        // This is a second user. Also shouldn't work.
        Assert.assertFalse(locationReservationDao.addLocationReservationIfStillRoomAtomically(lr));

        // It really really shouldn't be in the database.
        List<LocationReservation> reservations = locationReservationDao.getAllLocationReservationsOfUser(u2.getAugentID());
        Assert.assertEquals(0, reservations.size());
    }

    @Test
    public void getLocationReservationsAndCalendarPeriodOfUserTest() throws SQLException {
        User u = accountDao.getUserById(testUser.getAugentID()); // test user from db
        List<Pair<LocationReservation, CalendarPeriod>> elrs = new ArrayList<>(); // expected location reservations

        // Create first location reservation for user in testLocation
        CalendarPeriod cp0 = calendarPeriods.get(0);
        Timeslot t0 = cp0.getTimeslots().get(0);
        LocationReservation lr0 = new LocationReservation(u, LocalDateTime.now(), t0, null);
        elrs.add(new Pair<>(lr0, cp0));

        // Create a second location reservation for the user in testLocation2
        CalendarPeriod cp1 = calendarPeriodsForLocation2.get(1);
        Timeslot t1 = cp1.getTimeslots().get(1);
        LocationReservation lr1 = new LocationReservation(u, LocalDateTime.now(), t1, null);
        elrs.add(new Pair<>(lr1, cp1));

        // Add the location reservations to the db
        Assert.assertTrue(locationReservationDao.addLocationReservationIfStillRoomAtomically(lr0));
        Assert.assertTrue(locationReservationDao.addLocationReservationIfStillRoomAtomically(lr1));

        // Retrieve location reservations in combination with the locations
        List<Pair<LocationReservation, CalendarPeriod>> rlrs = locationReservationDao
                .getAllLocationReservationsAndCalendarPeriodsOfUser(u.getAugentID());

        // Sort expected and retrieved location reservations
        elrs.sort(Comparator.comparing(Pair::hashCode));
        rlrs.sort(Comparator.comparing(Pair::hashCode));

        Assert.assertEquals(elrs, rlrs);
    }

    // @Test
    public void scanStudentTest() throws SQLException {
        // retrieve entries from database instead of using the added instances
        Location location = locationDao.getLocation(testLocation.getName());
        User u1 = accountDao.getUserById(testUser.getAugentID());
        User u2 = accountDao.getUserById(testUser2.getAugentID());
        Timeslot timeslot = calendarPeriods.get(0).getTimeslots().get(0);

        // check whether all retrieved instances equal to the added instances
        Assert.assertEquals("scanStudentTest, setup testLocation", testLocation, location);
        Assert.assertEquals("scanStudentTest, setup testUser", testUser, u1);
        Assert.assertEquals("scanStudentTest, setup testUser2", testUser2, u2);

        // Make reservation for today
        LocalDate today = LocalDate.now();

        // Make reservations for users u1 and u2
        LocationReservation lr1 = new LocationReservation(u1, LocalDateTime.now(), timeslot, null);
        LocationReservation lr2 = new LocationReservation(u2, LocalDateTime.now(), timeslot, null);

        locationReservationDao.addLocationReservation(lr1);
        locationReservationDao.addLocationReservation(lr2);

        // count reserved seats
        long c = locationReservationDao.countReservedSeatsOfTimeslot(timeslot);
        Assert.assertEquals("scanStudentTest, count reserved seats", 2, c);

        // scan the users for the location on date
        locationReservationDao.scanStudent(testLocation.getName(), u1.getAugentID());
        LocationReservation rlr1 = locationReservationDao.getLocationReservation(u1.getAugentID(), timeslot);
        lr1.setAttended(true);
        Assert.assertEquals("scanStudentTest, u1 scanned", lr1, rlr1);

        // get present students
        List<LocationReservation> present = locationReservationDao.getPresentStudents(testLocation.getName(), today);
        Assert.assertEquals("scanStudentTest, present size", 1, present.size());
        Assert.assertEquals("scanStudentTest, present user", u1, present.get(0).getUser());

        // get absent students
        List<LocationReservation> absent = locationReservationDao.getAbsentStudents(testLocation.getName(), today);
        Assert.assertEquals("scanStudentTest, absent size", 1, absent.size());
        Assert.assertEquals("scanStudentTest, absent user", u2, absent.get(0).getUser());

        // set all students' attended = true for the date
        locationReservationDao.setAllStudentsOfLocationToAttended(testLocation.getName(), today);
        present = locationReservationDao.getPresentStudents(testLocation.getName(), today);
        Assert.assertEquals("scanStudentTest, present size after all attended", 2, present.size());
    }
}
