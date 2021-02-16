package blok2.daos.cascade;

import blok2.daos.*;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import blok2.model.reservations.LockerReservation;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

public class TestCascadeOfLocker extends TestDao {

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    @Autowired
    private ILockerReservationDao lockerReservationDao;

    private Location testLocation;
    private User testUser;
    private Locker locker;
    private LockerReservation testLockerReservation;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation = TestSharedMethods.testLocation(authority, testBuilding);

        testUser = TestSharedMethods.studentTestUser();

        locker = new Locker(0, testLocation);
        testLockerReservation = new LockerReservation(locker, testUser);

        // Add test objects to database
        locationDao.addLocation(testLocation);
        accountDao.directlyAddUser(testUser);
        lockerReservationDao.addLockerReservation(testLockerReservation);
    }

    @Test
    public void deleteLockerCascadeTest() throws SQLException {
        // are values in db
        Assert.assertEquals(testLocation, locationDao.getLocationByName(testLocation.getName()));
        Assert.assertEquals(testUser, accountDao.getUserById(testUser.getAugentID()));
        List<Locker> lockers = locationDao.getLockers(testLocation.getLocationId());
        Assert.assertEquals(testLocation.getNumberOfLockers(), lockers.size());
        Assert.assertEquals(testLockerReservation,
                lockerReservationDao.getLockerReservation(testLocation.getName(), 0));

        // delete locker
        locationDao.deleteLocker(testLocation.getLocationId(), locker.getNumber());

        // there should be one locker less
        Assert.assertEquals(lockers.size() - 1, locationDao.getLockers(testLocation.getLocationId()).size());

        // and the reservation should be deleted too
        Assert.assertNull(lockerReservationDao.getLockerReservation(testLocation.getName(), 0));
    }
}
