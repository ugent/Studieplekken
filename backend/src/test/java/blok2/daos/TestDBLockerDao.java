package blok2.daos;

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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TestDBLockerDao extends TestDao {

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILockersDao lockersDao;

    @Autowired
    private ILockerReservationDao lockerReservationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    private Location testLocation;
    private Authority authority;
    private User testUser1;
    private List<Locker> testLockers;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.getAuthorityId());
        testUser1 = TestSharedMethods.employeeAdminTestUser();
        testLockers = new ArrayList<>();

        // Add test object to database
        locationDao.addLocation(testLocation);
        TestSharedMethods.addTestUsers(accountDao, testUser1);

        for (int i = 0; i < testLocation.getNumberOfLockers(); i++) {
            // Create test lockers
            Locker locker = new Locker();
            locker.setNumber(i);
            locker.setLocation(testLocation);

            testLockers.add(locker);
        }
    }

    @Test
    public void getLockerStatusesOfLocationTest() throws SQLException {
        /*
        Scenario: Reserve a single locker in the location and check that the method return
        NumberOfLockers - 1 results and that the reserved locker is not in the results. In that case the filter works.
         */
        // Get random number of locker to reserve
        Random r = new Random();
        int random = r.nextInt(testLocation.getNumberOfLockers());

        // Reserve random locker
        Locker locker = testLockers.get(random);
        LockerReservation lr0 = new LockerReservation(locker, testUser1);
        lockerReservationDao.addLockerReservation(lr0);

        // Retrieve the locker statuses and the corresponding lockers that are returned
        List<LockerReservation> lockerStatusesOfLocation = lockersDao.getLockerStatusesOfLocation(testLocation.getName());
        List<Locker> resultLockers = lockerStatusesOfLocation.stream()
                .map(lr -> lr.getLocker())
                .collect(Collectors.toList());

        Assert.assertEquals("getLockerStatusesOfLocationTest", testLocation.getNumberOfLockers() - 1, lockerStatusesOfLocation.size());
        Assert.assertFalse(resultLockers.contains(locker));

    }
}
