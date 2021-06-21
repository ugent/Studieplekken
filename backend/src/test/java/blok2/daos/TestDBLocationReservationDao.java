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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class TestDBLocationReservationDao extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(TestDBLocationReservationDao.class);

    @Autowired
    private ILocationReservationDao locationReservationDao;

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private ITimeslotDAO timeslotDAO;

    @Autowired
    private IBuildingDao buildingDao;

    private Location testLocation;
    private User testUser;
    private User testUser2;
    private List<Pair<CalendarPeriod, List<Timeslot>>> calendarPeriods;
    private Pair<CalendarPeriod, List<Timeslot>> calendarPeriod1Seat;
    private List<Pair<CalendarPeriod, List<Timeslot>>> calendarPeriodsForLocation2;

    @Override
    public void populateDatabase() throws SQLException {
        // setup test location objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        Location testLocation1Seat = TestSharedMethods.testLocation1Seat(authority.clone(), testBuilding);
        Location testLocation2 = TestSharedMethods.testLocation2(authority.clone(), testBuilding);
        locationDao.addLocation(testLocation1Seat);
        locationDao.addLocation(testLocation2);
        locationDao.addLocation(testLocation);

        testUser = TestSharedMethods.adminTestUser();
        testUser2 = TestSharedMethods.studentTestUser();

        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);
        calendarPeriod1Seat = TestSharedMethods.testCalendarPeriods(testLocation1Seat).get(0);
        calendarPeriodsForLocation2 = TestSharedMethods.testCalendarPeriods(testLocation2);

        // Add test objects to database
        TestSharedMethods.addTestUsers(accountDao, testUser, testUser2);


        for (Pair<CalendarPeriod, List<Timeslot>> c : calendarPeriods) {
            TestSharedMethods.addPair(timeslotDAO, c);
        }
        for (Pair<CalendarPeriod, List<Timeslot>> c : calendarPeriodsForLocation2) {
            TestSharedMethods.addPair(timeslotDAO, c);
        }

        TestSharedMethods.addPair(timeslotDAO, calendarPeriod1Seat);
    }

    @Test
    public void addLocationReservationTest() throws SQLException {
        // retrieve entries from database instead of using the added instances
        Location location = locationDao.getLocationByName(testLocation.getName());
        User u = accountDao.getUserById(testUser.getAugentID());
        Timeslot timeslot = calendarPeriods.get(0).getSecond().get(0);
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

        Timeslot timeslot = calendarPeriod1Seat.getSecond().get(0);

        LocationReservation lr = new LocationReservation(u, LocalDateTime.now(), timeslot, null);
        TestSharedMethods.addPair(timeslotDAO, calendarPeriods.get(0));
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


    /**
     * Steps undertaken in this test:
     *     - create 500 test users
     *     - create a location that has 490 seats
     *     - create an upcoming calendar period
     *     - let each user make a reservation concurrently
     *     - test whether no constraints have been violated
     */
    //@Test
    public void concurrentReservationsTest() throws SQLException, InterruptedException {
        // some constants
        final int N_USERS = 50;
        final int N_SEATS = 46;

        // some variables that will be used
        Thread[] threads = new Thread[N_USERS];
        User[] users = new User[N_USERS];

        // Create N_USERS test users
        for (int i = 0; i < N_USERS; i++) {
            users[i] = TestSharedMethods.studentTestUser();
            users[i].setAugentID(users[i].getAugentID() + "" + i);
            users[i].setMail(i + "" + users[i].getMail());
            accountDao.directlyAddUser(users[i]);
        }
        logger.info(String.format("All %d users have been created.", N_USERS));

        // Create a location that has N_SEATS seats
        Building building = TestSharedMethods.testBuilding();
        building.setName("Building to test concurrent reservations");
        buildingDao.addBuilding(building);

        Location location = TestSharedMethods.testLocation(
                TestSharedMethods.insertTestAuthority("Test concurrent reservations",
                        "Authority to test concurrent reservations", authorityDao),
                building);
        location.setNumberOfSeats(N_SEATS);
        location.setName("Location to test concurrent reservations");
        locationDao.addLocation(location);
        logger.info("Location has been created");

        // Create an upcoming calendar period (timeslots will be created too)
        Pair<CalendarPeriod, List<Timeslot>> calendarPeriod = TestSharedMethods.upcomingCalendarPeriods(location);
        TestSharedMethods.addPair(timeslotDAO, calendarPeriod);
        logger.info("Calendar period has been created");

        Timeslot timeslot = calendarPeriod.getSecond().get(0);
        Assert.assertNotNull(timeslot);

        // Let each user make a reservation concurrently
        for (int i = 0; i < N_USERS; i++) {
            final int _i = i;
            threads[i] = new Thread(
                () -> {
                    LocationReservation lr = new LocationReservation(users[_i], LocalDateTime.now(), timeslot, null);
                    try {
                        boolean s = locationReservationDao.addLocationReservationIfStillRoomAtomically(lr);
                        if(!s)
                            System.out.println("didn't work in thread "+ _i);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            );

        }

        for (Thread thread: threads) {
            thread.start();
            logger.info("thread %3d has been started");

        }


        // Now, wait for all threads to be finished
        for (int i = 0; i < N_USERS; i++) {
            threads[i].join();
        }

        // Test whether no constraints have been violated
        long lrsCount = locationReservationDao.countReservedSeatsOfTimeslot(timeslot);
        Assert.assertEquals(N_SEATS, lrsCount);
    }
}
