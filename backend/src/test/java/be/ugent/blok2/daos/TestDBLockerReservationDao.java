package be.ugent.blok2.daos;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservables.Locker;
import be.ugent.blok2.reservations.LockerReservation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestDBLockerReservationDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private ILockerReservationDao lockerReservationDao;

    private Location testLocation;
    private User testUser1;
    private User testUser2;

    @Before
    public void setup() {
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(lockerReservationDao);

        testLocation = TestSharedMethods.testLocation();
        testUser1 = TestSharedMethods.employeeAdminTestUser();
        testUser2 = TestSharedMethods.studentEmployeeTestUser();
    }

    @After
    public void cleanup() {
        locationDao.useDefaultDatabaseConnection();
    }

    @Test
    // testing add/delete/change in one test
    public void lockerReservationTest() {
        // setup test
        TestSharedMethods.addTestUsers(accountDao, testUser1, testUser2);
        locationDao.addLocation(testLocation);

        // test whether users were correctly added to the database
        User u1 = accountDao.getUserById(testUser1.getAugentID());
        User u2 = accountDao.getUserById(testUser2.getAugentID());
        Assert.assertEquals("lockerReservationTest, setup testUser1", testUser1, u1);
        Assert.assertEquals("lockerReservationTest, setup testUser2", testUser2, u2);

        Locker[] lockers = (Locker[]) locationDao.getLockers(testLocation.getName()).toArray();

        if (lockers.length < 3)
            Assert.fail("Can't test without at least available lockers. Raise the testLocation.numberOfLockers");

        Locker locker1 = lockers[0];
        Locker locker2 = lockers[1];

        LockerReservation lr1 = new LockerReservation(locker1, u1);
        LockerReservation lr2 = new LockerReservation(locker2, u2);

        int usedLockers = lockerReservationDao.getNumberOfLockersInUseOfLocation(testLocation.getName());
        Assert.assertEquals("lockerReservationTest, usedLockers without any reservation", 0, usedLockers);

        lockerReservationDao.addLockerReservation(lr1);
        lockerReservationDao.addLockerReservation(lr2);

        usedLockers = lockerReservationDao.getNumberOfLockersInUseOfLocation(testLocation.getName());
        Assert.assertEquals("lockerReservationTest, usedLockers after reservations", 0, usedLockers);

        lr1.setKeyPickupDate(new CustomDate(1970, 1, 1));
        lr2.setKeyPickupDate(new CustomDate(1970, 1, 1));

        lockerReservationDao.changeLockerReservation(lr1);
        lockerReservationDao.changeLockerReservation(lr2);

        usedLockers = lockerReservationDao.getNumberOfLockersInUseOfLocation(testLocation.getName());
        Assert.assertEquals("lockerReservationTest, usedLockers after reservations and keys " +
                "picked up", 2, usedLockers);

        lr1.setKeyReturnedDate(new CustomDate(1970, 1, 31));
        lr2.setKeyReturnedDate(new CustomDate(1970, 1, 31));

        lockerReservationDao.changeLockerReservation(lr1);
        lockerReservationDao.changeLockerReservation(lr2);

        usedLockers = lockerReservationDao.getNumberOfLockersInUseOfLocation(testLocation.getName());
        Assert.assertEquals("lockerReservationTest, usedLockers after reservations and keys " +
                "picked up and returned again", 0, usedLockers);

        // rollback test
        locationDao.deleteLocation(testLocation.getName());
        TestSharedMethods.removeTestUsers(accountDao, testUser2, testUser1);
    }
}