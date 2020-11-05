package blok2.daos;

import blok2.helpers.date.CustomDate;
import blok2.model.Authority;
import blok2.model.Building;
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
    private IBuildingDao buildingDao;

    private Location testLocation;
    private Building testBuilding;
    private User testUser;
    private User testUser2;

    @Override
    public void populateDatabase() throws SQLException {
        // setup test location objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        testUser = TestSharedMethods.adminTestUser();
        testUser2 = TestSharedMethods.studentTestUser();

        // Add test objects to database
        TestSharedMethods.addTestUsers(accountDao, testUser, testUser2);
        locationDao.addLocation(testLocation);
    }

    @Test
    public void addLocationReservationTest() throws SQLException {
        // retrieve entries from database instead of using the added instances
        Location location = locationDao.getLocation(testLocation.getName());
        User u = accountDao.getUserById(testUser.getAugentID());

        // check whether all retrieved instances equal to the added instances
        Assert.assertEquals("addLocationReservation, setup testLocation", testLocation, location);
        Assert.assertEquals("addLocationReservation, setup testUser", testUser, u);

        // Create LocationReservation
        CustomDate date = new CustomDate(1970, 1, 1, 9, 0, 0);
        LocationReservation lr = new LocationReservation(location, u, date);

        // add LocationReservation to database
        locationReservationDao.addLocationReservation(lr);

        // test whether LocationReservation has been added successfully
        LocationReservation rlr = locationReservationDao.getLocationReservation(u.getAugentID(), date); // rlr = retrieved location reservation
        Assert.assertEquals("addLocationReservation, getLocationReservation", lr, rlr);

        List<LocationReservation> list = locationReservationDao.getAllLocationReservationsOfLocation(testLocation.getName(), true);
        Assert.assertEquals("addLocationReservation, getAllLocationReservationsOfLocation", 1, list.size());

        list = locationReservationDao.getAllLocationReservationsOfUser(u.getAugentID());
        Assert.assertEquals("addLocationReservation, getAllLocationReservationsOfUser", 1, list.size());

        // delete LocationReservation from database
        locationReservationDao.deleteLocationReservation(u.getAugentID(), date);
        rlr = locationReservationDao.getLocationReservation(u.getAugentID(), date);
        Assert.assertNull("addLocationReservationTest, delete LocationReservation", rlr);
    }

    @Test
    public void scanStudentTest() throws SQLException {
        // retrieve entries from database instead of using the added instances
        Location location = locationDao.getLocation(testLocation.getName());
        User u1 = accountDao.getUserById(testUser.getAugentID());
        User u2 = accountDao.getUserById(testUser2.getAugentID());

        // check whether all retrieved instances equal to the added instances
        Assert.assertEquals("scanStudentTest, setup testLocation", testLocation, location);
        Assert.assertEquals("scanStudentTest, setup testUser", testUser, u1);
        Assert.assertEquals("scanStudentTest, setup testUser2", testUser2, u2);

        // Make reservation for today
        CustomDate today = CustomDate.today();

        // Make reservations for users u1 and u2
        LocationReservation lr1 = new LocationReservation(location, u1, today);
        LocationReservation lr2 = new LocationReservation(location, u2, today);

        locationReservationDao.addLocationReservation(lr1);
        locationReservationDao.addLocationReservation(lr2);

        // count reserved seats
        int c = locationReservationDao.countReservedSeatsOfLocationOnDate(testLocation.getName(), today);
        Assert.assertEquals("scanStudentTest, count reserved seats", 2, c);

        // scan the users for the location on date
        locationReservationDao.scanStudent(testLocation.getName(), u1.getAugentID());
        LocationReservation rlr1 = locationReservationDao.getLocationReservation(u1.getAugentID(), today);
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
