package blok2.daos.cascade;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.daos.*;
import blok2.helpers.Language;
import blok2.helpers.Pair;
import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.Building;
import blok2.model.calendar.Timeslot;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;
import blok2.model.reservables.Location;
import blok2.model.reservables.Locker;
import blok2.model.reservations.LocationReservation;
import blok2.model.reservations.LockerReservation;
import blok2.model.users.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TestCascadeInDBAccountDao extends BaseTest {

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

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired IBuildingDao buildingDao;

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

    private Penalty testPenalty1;
    private Penalty testPenalty2;

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
        Pair<CalendarPeriod, List<Timeslot>> cp1 = TestSharedMethods.testCalendarPeriods(testLocation1).get(0);
        TestSharedMethods.addPair(calendarPeriodDao, cp1);
        Pair<CalendarPeriod, List<Timeslot>> cp2 = TestSharedMethods.testCalendarPeriods(testLocation2).get(0);
        TestSharedMethods.addPair(calendarPeriodDao, cp2);

        testLocationReservation1 = new LocationReservation(testUser, LocalDateTime.now(), cp1.getSecond().get(0),  null);
        testLocationReservation2 = new LocationReservation(testUser, LocalDateTime.of(1970,1,1,0,0), cp2.getSecond().get(0),  null);

        Locker testLocker1 = new Locker(0, testLocation1);
        Locker testLocker2 = new Locker(0, testLocation2);

        testLockerReservation1 = new LockerReservation(testLocker1, testUser);
        testLockerReservation2 = new LockerReservation(testLocker2, testUser);

        Map<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH, "Dit is een test omschrijving van een penalty event met code 0");
        descriptions.put(Language.ENGLISH, "This is a test description of a penalty event with code 0");

        // Note: the received amount of points are 10 and 20, not testPenaltyEvent.getCode()
        // because when the penalties are retrieved from the penaltyEventDao, the list will
        // be sorted by received points before asserting, if they would be equal we can't sort
        // on the points and be sure about the equality of the actual and expected list.

        PenaltyEvent testPenaltyEvent = new PenaltyEvent(0, 10, descriptions);
        testPenalty1 = new Penalty(testUser.getAugentID(), testPenaltyEvent.getCode(), LocalDate.now(), LocalDate.now(), testLocation1.getLocationId(), 10, "First test penalty");
        testPenalty2 = new Penalty(testUser.getAugentID(), testPenaltyEvent.getCode(), LocalDate.of(1970, 1, 1), LocalDate.now(), testLocation2.getLocationId(), 20, "Second test penalty");

        // Add test objects to database
        accountDao.directlyAddUser(testUser);


        locationReservationDao.addLocationReservation(testLocationReservation1);
        locationReservationDao.addLocationReservation(testLocationReservation2);

        lockerReservationDao.addLockerReservation(testLockerReservation1);
        lockerReservationDao.addLockerReservation(testLockerReservation2);

        penaltyEventsDao.addPenaltyEvent(testPenaltyEvent);
        penaltyEventsDao.addPenalty(testPenalty1);
        penaltyEventsDao.addPenalty(testPenalty2);

        scannerLocationDao.addScannerLocation(testLocation1.getLocationId(), testUser.getAugentID());
        scannerLocationDao.addScannerLocation(testLocation2.getLocationId(), testUser.getAugentID());
    }

    @Test
    public void updateUserWithoutCascadeNeededTest() throws SQLException {
        updateUserFieldWithoutAUGentID(testUser);
        accountDao.updateUserById(testUser.getAugentID(), testUser);
        User u = accountDao.getUserById(testUser.getAugentID());
        Assert.assertEquals("updateUserWithoutCascadeNeededTest", testUser, u);

        LocationReservation lr1 = locationReservationDao.getLocationReservation(
                testLocationReservation1.getUser().getAugentID(),
                testLocationReservation1.getTimeslot());
        Assert.assertEquals("updateUserWithoutCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(
                testLocationReservation2.getUser().getAugentID(),
                testLocationReservation2.getTimeslot());
        Assert.assertEquals("updateUserWithoutCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);
/*
        LockerReservation lor1 = lockerReservationDao.getLockerReservation(
                testLockerReservation1.getLocker().getLocationByName().getName(),
                testLockerReservation1.getLocker().getNumber());
        Assert.assertEquals("updateUserWithoutCascadeNeededTest, testLockerReservation1",
                testLockerReservation1, lor1);

        LockerReservation lor2 = lockerReservationDao.getLockerReservation(
                testLockerReservation2.getLocker().getLocationByName().getName(),
                testLockerReservation2.getLocker().getNumber());
        Assert.assertEquals("updateUserWithoutCascadeNeededTest, testLockerReservation2",
                testLockerReservation2, lor2);
*/
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByUser(testUser.getAugentID());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, penalties", expectedPenalties,
                penalties);

        List<Location> scannerLocations = scannerLocationDao.getLocationsToScanOfUser(testUser.getAugentID());
        scannerLocations.sort(Comparator.comparing(Location::getName));

        List<Location> expectedLocations = new ArrayList<>();
        expectedLocations.add(testLocation1);
        expectedLocations.add(testLocation2);
        expectedLocations.sort(Comparator.comparing(Location::getName));

        Assert.assertEquals("updateUserWithoutCascadeNeededTest, locations to scan with new id",
                expectedLocations, scannerLocations);
    }

    @Test
    public void updateUserWithCascadeNeededTest() throws SQLException {
        updateUserFieldWithoutAUGentID(testUser);
        String oldAUGentID = testUser.getAugentID();
        testUser.setAugentID(testUser.getAugentID() + "Iets in een test");
        accountDao.updateUserById(oldAUGentID, testUser);

        // the User needs to be updated in the first place
        User u = accountDao.getUserById(testUser.getAugentID());
        Assert.assertEquals("updateUserWithCascadeNeededTest, user", testUser, u);

        // the User with the old augentid needs to be removed
        u = accountDao.getUserById(oldAUGentID);
        Assert.assertNull("updateUserWithCascadeNeededTest, old user needs to be removed", u);

        // check whether the entries in LOCATION_RESERVATIONS have been updated in cascade
        // note that because of references, the User object in testLocationReservation1/2
        // have the updated testUser
        LocationReservation lr1 = locationReservationDao.getLocationReservation(
                testLocationReservation1.getUser().getAugentID(),
                testLocationReservation1.getTimeslot());
        Assert.assertEquals("updateUserWithCascadeNeededTest, testLocationReservation1",
                testLocationReservation1, lr1);

        LocationReservation lr2 = locationReservationDao.getLocationReservation(
                testLocationReservation2.getUser().getAugentID(),
                testLocationReservation2.getTimeslot());
        Assert.assertEquals("updateUserWithCascadeNeededTest, testLocationReservation2",
                testLocationReservation2, lr2);
/*
        // check whether the entries in LOCKER_RESERVATIONS have been updated in cascade
        LockerReservation lor1 = lockerReservationDao.getLockerReservation(
                testLockerReservation1.getLocker().getLocationByName().getName(),
                testLockerReservation1.getLocker().getNumber());
        Assert.assertEquals("updateUserWithCascadeNeededTest, testLockerReservation1",
                testLockerReservation1, lor1);

        LockerReservation lor2 = lockerReservationDao.getLockerReservation(
                testLockerReservation2.getLocker().getLocationByName().getName(),
                testLockerReservation2.getLocker().getNumber());
        Assert.assertEquals("updateUserWithCascadeNeededTest, testLockerReservation2",
                testLockerReservation2, lor2);
*/
        // check whether the entries in PENALTY_BOOK have been updated in cascade
        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByUser(testUser.getAugentID());
        penalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        // Penalty objects don't keep a reference to User, but have a String with the augentid
        testPenalty1.setAugentID(testUser.getAugentID());
        testPenalty2.setAugentID(testUser.getAugentID());

        List<Penalty> expectedPenalties = new ArrayList<>();
        expectedPenalties.add(testPenalty1);
        expectedPenalties.add(testPenalty2);
        expectedPenalties.sort(Comparator.comparing(Penalty::getReceivedPoints));

        Assert.assertEquals("updateUserWithCascadeNeededTest, penalties", expectedPenalties,
                penalties);

        // check whether the entries in SCANNERS_LOCATION have been updated in cascade
        List<Location> scannerLocations = scannerLocationDao.getLocationsToScanOfUser(oldAUGentID);
        Assert.assertEquals("updateUserWithCascadeNeededTest, locations to scan with old id",
                0, scannerLocations.size());

        scannerLocations = scannerLocationDao.getLocationsToScanOfUser(testUser.getAugentID());
        scannerLocations.sort(Comparator.comparing(Location::getName));

        List<Location> expectedLocations = new ArrayList<>();
        expectedLocations.add(testLocation1);
        expectedLocations.add(testLocation2);
        expectedLocations.sort(Comparator.comparing(Location::getName));

        Assert.assertEquals("updateUserWithCascadeNeededTest, locations to scan with new id",
                expectedLocations, scannerLocations);
    }

    @Test
    public void deleteUserTest() throws SQLException {
        accountDao.deleteUser(testUser.getAugentID());
        User u = accountDao.getUserById(testUser.getAugentID());
        Assert.assertNull("deleteUserTest, user must be deleted", u);

        List<Location> scannerLocations = scannerLocationDao.getLocationsToScanOfUser(testUser.getAugentID());
        Assert.assertEquals("deleteUserTest, scannerLocations", 0,
                scannerLocations.size());

        List<Penalty> penalties = penaltyEventsDao.getPenaltiesByUser(testUser.getAugentID());
        Assert.assertEquals("deleteUserTest, penalties", 0, penalties.size());

        List<LocationReservation> locationReservations = locationReservationDao
                .getAllLocationReservationsOfUser(testUser.getAugentID());
        Assert.assertEquals("deleteUserTest, location reservations", 0,
                locationReservations.size());
/*
        List<LockerReservation> lockerReservations = lockerReservationDao
                .getAllLockerReservationsOfUser(testUser.getAugentID());
        Assert.assertEquals("deleteUserTest, locker reservations", 0,
                lockerReservations.size());
    */
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
