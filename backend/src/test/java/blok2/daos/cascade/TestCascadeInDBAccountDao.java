package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.database.dao.*;
import blok2.exception.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.Timeslot;
import blok2.model.penalty.Penalty;
import blok2.model.location.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.*;

public class TestCascadeInDBAccountDao extends BaseTest {

    @Autowired
    private IUserDao userDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private ILocationReservationDao locationReservationDao;

    @Autowired
    private IPenaltyDao penaltyDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private ITimeslotDao timeslotDAO;

    @Autowired
    IBuildingDao buildingDao;

    // this will be the test user
    private User testUser;

    // for cascade on SCANNERS_LOCATION, LOCATION_RESERVATIONS
    // and LOCKER_RESERVATIONS, a Location must be available
    private Location testLocation1;
    private Location testLocation2;

    // to test cascade on LOCATION_RESERVATIONS
    private LocationReservation testLocationReservation1;
    private LocationReservation testLocationReservation2;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        testUser = TestSharedMethods.studentTestUser();

        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation1 = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        testLocation2 = TestSharedMethods.testLocation2(authority.clone(), testBuilding);

        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);
        List<Timeslot> cp1 = TestSharedMethods.testCalendarPeriods(testLocation1);
        cp1 = timeslotDAO.addTimeslots(cp1);
        List<Timeslot> cp2 = TestSharedMethods.testCalendarPeriods(testLocation2);
        cp2 = timeslotDAO.addTimeslots(cp2);

        testLocationReservation1 = new LocationReservation(testUser, cp1.get(0),  LocationReservation.State.APPROVED);
        testLocationReservation2 = new LocationReservation(testUser, cp2.get(0), LocationReservation.State.APPROVED);

        // Add test objects to database
        userDao.addUser(testUser);

        locationReservationDao.addLocationReservation(testLocationReservation1);
        locationReservationDao.addLocationReservation(testLocationReservation2);
    }

    @Test
    public void updateUserTest() {
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
                testLocationReservation2, lr2);}

    @Test
    public void deleteUserTest() {
        userDao.deleteUser(testUser.getUserId());
        try {
            userDao.getUserById(testUser.getUserId());
            Assert.fail("user must be deleted");
        } catch (NoSuchDatabaseObjectException e) {
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
