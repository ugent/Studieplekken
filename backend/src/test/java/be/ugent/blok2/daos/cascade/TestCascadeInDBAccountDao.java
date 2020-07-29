package be.ugent.blok2.daos.cascade;

import be.ugent.blok2.TestSharedMethods;
import be.ugent.blok2.daos.*;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservables.Locker;
import be.ugent.blok2.model.reservations.LocationReservation;
import be.ugent.blok2.model.reservations.LockerReservation;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"db", "test"})
public class TestCascadeInDBAccountDao {

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private ILocationReservationDao locationReservationDao;

    @Autowired
    private ILockerReservationDao lockerReservationDao;

    @Autowired
    private IPenaltyEventsDao penaltyEventsDao;

    @Autowired
    private IScannerLocationDao scannerLocationDao;

    // this will be the test user
    private User testUser;

    // for cascade on SCANNERS_LOCATION, LOCATION_RESERVATIONS
    // and LOCKER_RESERVATIONS, a Location must be available
    private Location testLocation1;
    private Location testLocation2;

    // to test cascade on LOCATION_RESERVATIONS
    private LocationReservation testLocationReservation1;
    private LocationReservation testLocationReservation2;

    // to test cascade on LOCKER_RESERVATIONS
    private LockerReservation testLockerReservation1;
    private LockerReservation testLockerReservation2;

    // to test cascade on PENALTY_BOOK
    private PenaltyEvent testPenaltyEvent;
    private Penalty testPenalty1;
    private Penalty testPenalty2;

    @Before
    public void setup() throws SQLException {
        // Use test database
        TestSharedMethods.setupTestDaoDatabaseCredentials(accountDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(locationReservationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(lockerReservationDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(penaltyEventsDao);
        TestSharedMethods.setupTestDaoDatabaseCredentials(scannerLocationDao);

        // Setup test objects
        testUser = TestSharedMethods.studentEmployeeTestUser();
        testLocation1 = TestSharedMethods.testLocation();
        testLocation2 = TestSharedMethods.testLocation2();

        testLocationReservation1 = new LocationReservation(testLocation1, testUser, CustomDate.now());
        testLocationReservation2 = new LocationReservation(testLocation2, testUser, new CustomDate(1970, 1, 1));

        Locker testLocker1 = new Locker(0, testLocation1);
        Locker testLocker2 = new Locker(0, testLocation2);

        testLockerReservation1 = new LockerReservation(testLocker1, testUser);
        testLockerReservation2 = new LockerReservation(testLocker2, testUser);

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH, "Dit is een test omschrijving");
        descriptions.put(Language.ENGLISH, "This is a test description");
        testPenaltyEvent = new PenaltyEvent(0, 10, true, descriptions);

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting
        testPenalty1 = new Penalty(testUser.getAugentID(), testPenaltyEvent.getCode(), CustomDate.now(), CustomDate.now(), testLocation1.getName(), 10);
        testPenalty2 = new Penalty(testUser.getAugentID(), testPenaltyEvent.getCode(), new CustomDate(1970, 1, 1), CustomDate.now(), testLocation2.getName(), 20);

        // Add test objects to database
        accountDao.directlyAddUser(testUser);
        locationDao.addLocation(testLocation1);
        locationDao.addLocation(testLocation2);
        locationReservationDao.addLocationReservation(testLocationReservation1);
        locationReservationDao.addLocationReservation(testLocationReservation2);
        lockerReservationDao.addLockerReservation(testLockerReservation1);
        lockerReservationDao.addLockerReservation(testLockerReservation2);
        penaltyEventsDao.addPenaltyEvent(testPenaltyEvent);
        penaltyEventsDao.addPenalty(testPenalty1);
        penaltyEventsDao.addPenalty(testPenalty2);
        scannerLocationDao.addScannerLocation(testLocation1.getName(), testUser.getAugentID());
        scannerLocationDao.addScannerLocation(testLocation2.getName(), testUser.getAugentID());
    }

    @After
    public void cleanup() throws SQLException {
        // Remove test objects from database
        // Note that here, I am not relying on the cascade because that's
        // what we are testing here in this class
        penaltyEventsDao.deletePenalty(testPenalty2);
        penaltyEventsDao.deletePenalty(testPenalty1);
        penaltyEventsDao.deletePenaltyEvent(testPenaltyEvent.getCode());
        lockerReservationDao.deleteLockerReservation(testLockerReservation2.getLocker().getLocation().getName(),
                testLockerReservation2.getLocker().getNumber());
        lockerReservationDao.deleteLockerReservation(testLockerReservation1.getLocker().getLocation().getName(),
                testLockerReservation1.getLocker().getNumber());
        locationReservationDao.deleteLocationReservation(testLocationReservation2.getUser().getAugentID(),
                testLocationReservation2.getDate());
        locationReservationDao.deleteLocationReservation(testLocationReservation1.getUser().getAugentID(),
                testLocationReservation1.getDate());
        // okay, cascade is assumed to be okay for the lockers here... (but it does)
        locationDao.deleteLocation(testLocation2.getName());
        locationDao.deleteLocation(testLocation1.getName());
        accountDao.deleteUser(testUser.getAugentID());

        // Use regular database
        accountDao.useDefaultDatabaseConnection();
        locationDao.useDefaultDatabaseConnection();
        locationReservationDao.useDefaultDatabaseConnection();
        lockerReservationDao.useDefaultDatabaseConnection();
        penaltyEventsDao.useDefaultDatabaseConnection();
        scannerLocationDao.useDefaultDatabaseConnection();
    }

    @Test
    public void updateUserWithoutCascadeTest() throws SQLException {
        updateUserFieldWithoutAUGentID(testUser);
        accountDao.updateUserById(testUser.getAugentID(), testUser);
        User u = accountDao.getUserById(testUser.getAugentID());
        Assert.assertEquals("updateUserKeepFK", testUser, u);
    }

    @Test
    public void updateUserWithCascadeTest() throws SQLException {
        updateUserFieldWithoutAUGentID(testUser);
        String oldAUGentID = testUser.getAugentID();
        testUser.setAugentID(testUser.getAugentID() + "Iets in een test");
        accountDao.updateUserById(oldAUGentID, testUser);

        // the User needs to be updated in the first place
        User u = accountDao.getUserById(testUser.getAugentID());
        Assert.assertEquals("updateUserWithCascadeTest, user", testUser, u);

        // the User with the old augentid needs to be removed
        u = accountDao.getUserById(oldAUGentID);
        Assert.assertNull("updateUserWithCascadeTest, old user needs to be removed", u);

        // check whether the entries in LOCATION_RESERVATIONS have been updated in cascade
        // note that because of references, the User object in testLocationReservation1/2
        // have the updated testUser
        LocationReservation lr1 = locationReservationDao.getLocationReservation(
                testLocationReservation1.getUser().getAugentID(),
                testLocationReservation1.getDate());
        Assert.assertEquals("updateUserWithCascade, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(
                testLocationReservation2.getUser().getAugentID(),
                testLocationReservation2.getDate());
        Assert.assertEquals("updateUserWithCascade, testLocationReservation2",
                testLocationReservation2, lr2);

        // check whether the entries in LOCKER_RESERVATIONS have been updated in cascade
        LockerReservation lor1 = lockerReservationDao.getLockerReservation(
                testLockerReservation1.getLocker().getLocation().getName(),
                testLockerReservation1.getLocker().getNumber());
        Assert.assertEquals("updateUserWithCascade, testLockerReservation1",
                testLockerReservation1, lor1);

        LockerReservation lor2 = lockerReservationDao.getLockerReservation(
                testLockerReservation2.getLocker().getLocation().getName(),
                testLockerReservation2.getLocker().getNumber());
        Assert.assertEquals("updateUserWithCascade, testLockerReservation2",
                testLockerReservation2, lor2);

        // check whether the entries in PENALTY_BOOK have been updated in cascade
        List<Penalty> penalties = penaltyEventsDao.getPenalties(testUser.getAugentID());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        // PenaltyEvent doesn't keep a reference to User, but the augentid
        testPenalty1.setAugentID(testUser.getAugentID());
        testPenalty2.setAugentID(testUser.getAugentID());

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateUserWithCascade, penalties", expectedPenalties,
                penalties);

        // check whether the entries in SCANNERS_LOCATION
        List<Location> scannerLocations = scannerLocationDao.getLocationsToScanOfUser(oldAUGentID);
        Assert.assertEquals("updateUserWithCascade, locations to scan with old id",
                0, scannerLocations.size());

        scannerLocations = scannerLocationDao.getLocationsToScanOfUser(testUser.getAugentID());
        scannerLocations.sort(Comparator.comparing(Location::getName));

        List<Location> expectedLocations = new ArrayList<>();
        expectedLocations.add(testLocation1);
        expectedLocations.add(testLocation2);
        expectedLocations.sort(Comparator.comparing(Location::getName));

        Assert.assertEquals("updateUserWithCascade, locations to scan with new id",
                expectedLocations, scannerLocations);
    }

    @Test
    public void deleteUserTest() throws SQLException {
        accountDao.deleteUser(testUser.getAugentID());

        List<Location> scannerLocations = scannerLocationDao.getLocationsToScanOfUser(testUser.getAugentID());
        Assert.assertEquals("deleteUserTest, scannerLocations", 0,
                scannerLocations.size());

        List<Penalty> penalties = penaltyEventsDao.getPenalties(testUser.getAugentID());
        Assert.assertEquals("deleteUserTest, penalties", 0, penalties.size());

        List<LocationReservation> locationReservations = locationReservationDao
                .getAllLocationReservationsOfUser(testUser.getAugentID());
        Assert.assertEquals("deleteUserTest, location reservations", 0,
                locationReservations.size());

        List<LockerReservation> lockerReservations = lockerReservationDao
                .getAllLockerReservationsOfUser(testUser.getAugentID());
        Assert.assertEquals("deleteUserTest, locker reservations", 0,
                lockerReservations.size());
    }

    private void updateUserFieldWithoutAUGentID(User user) {
        user.setLastName("Changed last name");
        user.setFirstName("Changed first name");
        user.setMail("Changed.Mail@UGent.be");
        user.setPassword("Changed Password");
        user.setInstitution("UGent");
        user.setRoles(new Role[]{Role.STUDENT});
    }
}
