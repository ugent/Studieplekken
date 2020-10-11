package blok2.daos;

import blok2.helpers.date.CustomDate;
import blok2.model.Authority;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import blok2.model.reservations.LockerReservation;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TestDBLockerReservationDao extends TestDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private ILockerReservationDao lockerReservationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    private Location testLocation;
    private User testUser1;
    private User testUser2;
    private List<LockerReservation> testLockerReservations;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.clone());
        testUser1 = TestSharedMethods.employeeAdminTestUser();
        testUser2 = TestSharedMethods.studentEmployeeTestUser();

        testLockerReservations = new ArrayList<>();

        for (int i = 0; i < testLocation.getNumberOfLockers(); i++) {
            Locker locker = new Locker();
            locker.setNumber(i);
            locker.setLocation(testLocation);

            if (testLockerReservations.size() < 1)
                testLockerReservations.add(new LockerReservation(locker, testUser1));
            else if (testLockerReservations.size() < 2)
                testLockerReservations.add(new LockerReservation(locker, testUser2));
            else if (testLockerReservations.size() < 3)
                testLockerReservations.add(new LockerReservation(locker, testUser2));
        }

        // Add test objects to database
        TestSharedMethods.addTestUsers(accountDao, testUser1, testUser2);
        locationDao.addLocation(testLocation);
        lockerReservationDao.addLockerReservation(testLockerReservations.get(0));
        lockerReservationDao.addLockerReservation(testLockerReservations.get(1));
        lockerReservationDao.addLockerReservation(testLockerReservations.get(2));
    }

    @Test
    public void addLockerReservationTest() throws SQLException {
        // these three LockerReservations should've been added by @Begin
        LockerReservation lr0 = lockerReservationDao.getLockerReservation(
                testLockerReservations.get(0).getLocker().getLocation().getName(),
                testLockerReservations.get(0).getLocker().getNumber()
        );
        LockerReservation lr1 = lockerReservationDao.getLockerReservation(
                testLockerReservations.get(1).getLocker().getLocation().getName(),
                testLockerReservations.get(1).getLocker().getNumber()
        );
        LockerReservation lr2 = lockerReservationDao.getLockerReservation(
                testLockerReservations.get(2).getLocker().getLocation().getName(),
                testLockerReservations.get(2).getLocker().getNumber()
        );

        Assert.assertEquals("addLockerReservationTest", testLockerReservations.get(0), lr0);
        Assert.assertEquals("addLockerReservationTest", testLockerReservations.get(1), lr1);
        Assert.assertEquals("addLockerReservationTest", testLockerReservations.get(2), lr2);
    }

    @Test
    public void deleteLockerReservationTest() throws SQLException {
        lockerReservationDao.deleteLockerReservation(testLockerReservations.get(0).getLocker().getLocation().getName(),
                testLockerReservations.get(0).getLocker().getNumber());
        lockerReservationDao.deleteLockerReservation(testLockerReservations.get(1).getLocker().getLocation().getName(),
                testLockerReservations.get(1).getLocker().getNumber());
        lockerReservationDao.deleteLockerReservation(testLockerReservations.get(2).getLocker().getLocation().getName(),
                testLockerReservations.get(2).getLocker().getNumber());

        LockerReservation lr0 = lockerReservationDao.getLockerReservation(
                testLockerReservations.get(0).getLocker().getLocation().getName(),
                testLockerReservations.get(0).getLocker().getNumber()
        );
        LockerReservation lr1 = lockerReservationDao.getLockerReservation(
                testLockerReservations.get(1).getLocker().getLocation().getName(),
                testLockerReservations.get(1).getLocker().getNumber()
        );
        LockerReservation lr2 = lockerReservationDao.getLockerReservation(
                testLockerReservations.get(2).getLocker().getLocation().getName(),
                testLockerReservations.get(2).getLocker().getNumber()
        );

        Assert.assertNull("deleteLockerReservationTest", lr0);
        Assert.assertNull("deleteLockerReservationTest", lr1);
        Assert.assertNull("deleteLockerReservationTest", lr2);
    }

    @Test
    public void gettersTest() throws SQLException {
        // getAllLockerReservationsOfUser
        List<LockerReservation> reservations =
                lockerReservationDao.getAllLockerReservationsOfUser(testUser1.getAugentID());
        Assert.assertEquals("test getters, getAllLockerReservationsOfUser", 1, reservations.size());

        reservations = lockerReservationDao.getAllLockerReservationsOfUser(testUser2.getAugentID());
        Assert.assertEquals("test getters, getAllLockerReservationsOfUser", 2, reservations.size());

        // getAllLockerReservationsOfUserByName (3 cases: first name, last name, first + last name
        reservations = lockerReservationDao.getAllLockerReservationsOfUserByName(testUser2.getFirstName());
        Assert.assertEquals("test getters, getAllLockerReservationsOfUserByName",
                2, reservations.size());

        // Note: both testUser1 and testUser2 have the same last name
        reservations = lockerReservationDao.getAllLockerReservationsOfUserByName(testUser2.getLastName());
        Assert.assertEquals("test getters, getAllLockerReservationsOfUserByName",
                testLockerReservations.size(), reservations.size());

        reservations = lockerReservationDao
                .getAllLockerReservationsOfUserByName(testUser2.getFirstName() + " " + testUser2.getLastName());
        Assert.assertEquals("test getters, getAllLockerReservationsOfUserByName",
                2, reservations.size());

        // getAllLockerReservationsOfLocation
        reservations = lockerReservationDao.getAllLockerReservationsOfLocation(testLocation.getName(), true);
        Assert.assertEquals("test getters, getAllLockerReservationsOfLocation",
                testLockerReservations.size(), reservations.size());

        reservations.sort(Comparator.comparingInt(a -> a.getLocker().getNumber()));
        testLockerReservations.sort(Comparator.comparingInt(a -> a.getLocker().getNumber()));
        for (int i = 0; i < reservations.size(); i++) {
            Assert.assertEquals("test getters, getAllLockerReservationsOfLocation",
                    testLockerReservations.get(i), reservations.get(i));
        }

        // getAllLockerReservationsOfLocationWithoutKeyBroughtBack
        reservations = lockerReservationDao
                .getAllLockerReservationsOfLocationWithoutKeyBroughtBack(testLocation.getName());
        Assert.assertEquals("test getters, getAllLockerReservationsOfLocationWithoutKeyBroughtBack",
                testLockerReservations.size(), reservations.size());
    }

    @Test
    public void changeLockerReservationTest() throws SQLException {
        // set key pickup date
        testLockerReservations.get(0).setKeyPickupDate(CustomDate.now());
        testLockerReservations.get(1).setKeyPickupDate(CustomDate.now());
        testLockerReservations.get(2).setKeyPickupDate(CustomDate.now());

        // change first LockerReservation
        lockerReservationDao.changeLockerReservation(testLockerReservations.get(0));
        int lockersInUse = lockerReservationDao.getNumberOfLockersInUseOfLocation(testLocation.getName());
        Assert.assertEquals("changeLockerReservation, picked up two keys", 1, lockersInUse);

        // change second LockerReservation
        lockerReservationDao.changeLockerReservation(testLockerReservations.get(1));
        lockersInUse = lockerReservationDao.getNumberOfLockersInUseOfLocation(testLocation.getName());
        Assert.assertEquals("changeLockerReservation, picked up two keys", 2, lockersInUse);

        // change third LockerReservation
        lockerReservationDao.changeLockerReservation(testLockerReservations.get(2));
        lockersInUse = lockerReservationDao.getNumberOfLockersInUseOfLocation(testLocation.getName());
        Assert.assertEquals("changeLockerReservation, picked up three keys", 3, lockersInUse);

        // set return date
        testLockerReservations.get(0).setKeyReturnedDate(CustomDate.now());
        testLockerReservations.get(1).setKeyReturnedDate(CustomDate.now());
        testLockerReservations.get(2).setKeyReturnedDate(CustomDate.now());

        // change all LockerReservations
        lockerReservationDao.changeLockerReservation(testLockerReservations.get(0));
        lockerReservationDao.changeLockerReservation(testLockerReservations.get(1));
        lockerReservationDao.changeLockerReservation(testLockerReservations.get(2));

        lockersInUse = lockerReservationDao.getNumberOfLockersInUseOfLocation(testLocation.getName());
        Assert.assertEquals("changeLockerReservation, returned all keys", 0, lockersInUse);
    }

}
