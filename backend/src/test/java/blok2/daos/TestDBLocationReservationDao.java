package blok2.daos;

import blok2.helpers.date.CustomDate;
import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

public class TestDBLocationReservationDao extends TestDao {

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


    private Location testLocation;

    private User testUser;
    private User testUser2;
    private List<CalendarPeriod> calendarPeriods;
    private CalendarPeriod calendarPeriod1Seat;

    @Override
    public void populateDatabase() throws SQLException {
        // setup test location objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.clone());
        Location testLocation1Seat = TestSharedMethods.testLocation1Seat(authority.clone());

        testUser = TestSharedMethods.adminTestUser();
        testUser2 = TestSharedMethods.studentTestUser();
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);
        calendarPeriod1Seat = TestSharedMethods.testCalendarPeriods(testLocation1Seat).get(0);

        // Add test objects to database
        TestSharedMethods.addTestUsers(accountDao, testUser, testUser2);
        locationDao.addLocation(testLocation);
        locationDao.addLocation(testLocation1Seat);

        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, calendarPeriods.get(0));
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, calendarPeriod1Seat);

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
        CustomDate date = new CustomDate(1970, 1, 1, 9, 0, 0);
        LocationReservation lr = new LocationReservation(u, date.toDateString(), timeslot, null);

        // add LocationReservation to database
        locationReservationDao.addLocationReservation(lr);

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

        LocationReservation lr = new LocationReservation(u, CustomDate.today().toDateString(), timeslot, null);
        TestSharedMethods.addCalendarPeriods(calendarPeriodDao, calendarPeriods.get(0));
        Assert.assertTrue(locationReservationDao.addLocationReservationIfStillRoomAtomically(lr));
        lr = new LocationReservation(u, CustomDate.today().toDateString(), timeslot, null);
        // This is a duplicate entry into the database. Shouldn't work.
        Assert.assertFalse(locationReservationDao.addLocationReservationIfStillRoomAtomically(lr));
        lr = new LocationReservation(u2, CustomDate.today().toDateString(), timeslot, null);
        // This is a second user. Also shouldn't work.
        Assert.assertFalse(locationReservationDao.addLocationReservationIfStillRoomAtomically(lr));

        // It really really shouldn't be in the database.
        List<LocationReservation> reservations = locationReservationDao.getAllLocationReservationsOfUser(u2.getAugentID());
        Assert.assertEquals(0, reservations.size());
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
        CustomDate today = CustomDate.today();

        // Make reservations for users u1 and u2
        LocationReservation lr1 = new LocationReservation(u1, CustomDate.today().toDateString(), timeslot, null);
        LocationReservation lr2 = new LocationReservation(u2, CustomDate.today().toDateString(), timeslot, null);

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

        // set u1 to unattended
        locationReservationDao.setReservationToUnAttended(u1.getAugentID(), today);
        present = locationReservationDao.getPresentStudents(testLocation.getName(), today);
        Assert.assertEquals("scanStudentTest, present size after u1 unattended", 1, present.size());
        Assert.assertEquals("scanStudentTest, present user after u1 unattended should be u2", u2, present.get(0).getUser());
    }
}
