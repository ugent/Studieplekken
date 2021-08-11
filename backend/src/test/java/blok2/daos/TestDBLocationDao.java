package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;


@FlywayTest
public class TestDBLocationDao extends BaseTest {

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    @Autowired
    private ITimeslotDAO timeslotDAO;

    private Location testLocation;

    @Override
    public void populateDatabase() {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);

        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @FlywayTest
    @Test
    public void addLocationTest() {
        Location l = locationDao.getLocationByName(testLocation.getName());
        Assert.assertEquals("addLocation", testLocation, l);

        locationDao.deleteLocation(testLocation.getLocationId());
        try {
            locationDao.getLocationByName(testLocation.getName());
        } catch (NoSuchDatabaseObjectException ignore) {
            Assert.assertTrue("Location must be deleted and thus a NoSuchDatabaseObjectException should have been thrown.", true);
        }
    }

    @Test
    public void currentTimeslotTestForLocationById() throws SQLException {
        // at this moment, no calendar periods are present, so currentTimeslot must be null
        Location location = locationDao.getLocationById(testLocation.getLocationId());
        Assert.assertNull(location.getCurrentTimeslot());

        // create a calendar period in the past
        List<Timeslot> pastCalendarPeriod = TestSharedMethods.pastCalendarPeriods(location);
        timeslotDAO.addTimeslots(pastCalendarPeriod);

        // still, no currentTimeslot should be initialized
        location = locationDao.getLocationById(testLocation.getLocationId());
        Assert.assertNull(location.getCurrentTimeslot());

        // create an upcoming calendar period
        List<Timeslot> upcomingCalendarPeriod = TestSharedMethods.upcomingCalendarPeriods(location);
        timeslotDAO.addTimeslots(upcomingCalendarPeriod);

        // A current timeslot should be initialized
        location = locationDao.getLocationById(testLocation.getLocationId());
        Assert.assertNotNull(location.getCurrentTimeslot());


        upcomingCalendarPeriod.forEach(t -> timeslotDAO.deleteTimeslot(t));
        // It should be uninitialized again.
        location = locationDao.getLocationById(testLocation.getLocationId());
        Assert.assertNull(location.getCurrentTimeslot());

        // create a calendar period for today, with a timeslot at this moment
        List<Timeslot> activeCPInsideHours = TestSharedMethods.activeCalendarPeriodsInsideHours(location);
        timeslotDAO.addTimeslots(activeCPInsideHours);

        // now, the currentTimeslot should be initialized
        location = locationDao.getLocationById(testLocation.getLocationId());
        Assert.assertNotNull(location.getCurrentTimeslot());
    }

    @Test
    public void currentTimeslotTestForLocationByName() throws SQLException {
        // at this moment, no calendar periods are present, so currentTimeslot must be null
        Location location = locationDao.getLocationByName(testLocation.getName());
        Assert.assertNull(location.getCurrentTimeslot());

        // create a calendar period in the past
        List<Timeslot> pastCalendarPeriod = TestSharedMethods.pastCalendarPeriods(location);
        timeslotDAO.addTimeslots(pastCalendarPeriod);

        // still, no currentTimeslot should be initialized
        location = locationDao.getLocationByName(testLocation.getName());
        Assert.assertNull(location.getCurrentTimeslot());

        // create an upcoming calendar period
        List<Timeslot> upcomingCalendarPeriod = TestSharedMethods.upcomingCalendarPeriods(location);
        timeslotDAO.addTimeslots(upcomingCalendarPeriod);

        // A current timeslot should be initialized
        location = locationDao.getLocationByName(testLocation.getName());
        Assert.assertNotNull(location.getCurrentTimeslot());


        upcomingCalendarPeriod.forEach(t -> timeslotDAO.deleteTimeslot(t));
        // It should be uninitialized again.
        location = locationDao.getLocationByName(testLocation.getName());
        Assert.assertNull(location.getCurrentTimeslot());

        // create a calendar period for today, with a timeslot at this moment
        List<Timeslot> activeCPInsideHours = TestSharedMethods.activeCalendarPeriodsInsideHours(location);
        timeslotDAO.addTimeslots(activeCPInsideHours);

        // now, the currentTimeslot should be initialized
        location = locationDao.getLocationByName(testLocation.getName());
        Assert.assertNotNull(location.getCurrentTimeslot());
    }

    @Test
    public void currentTimeslotTestForAllApprovedLocations() throws SQLException {
        // first, approve the location
        locationDao.approveLocation(testLocation, true);

        // at this moment, no calendar periods are present, so currentTimeslot must be null
        Location location = locationDao.getAllActiveLocations().get(0);
        Assert.assertNull(location.getCurrentTimeslot());

        // create a calendar period in the past
        List<Timeslot> pastCalendarPeriod = TestSharedMethods.pastCalendarPeriods(location);
        timeslotDAO.addTimeslots(pastCalendarPeriod);

        // still, no currentTimeslot should be initialized
        location = locationDao.getAllActiveLocations().get(0);
        Assert.assertNull(location.getCurrentTimeslot());

        // create an upcoming calendar period
        List<Timeslot> upcomingCalendarPeriod = TestSharedMethods.upcomingCalendarPeriods(location);
        timeslotDAO.addTimeslots(upcomingCalendarPeriod);

        // A current timeslot should be initialized
        location = locationDao.getAllActiveLocations().get(0);
        Assert.assertNotNull(location.getCurrentTimeslot());


        upcomingCalendarPeriod.forEach(t -> timeslotDAO.deleteTimeslot(t));
        // It should be uninitialized again.
        location = locationDao.getAllActiveLocations().get(0);
        Assert.assertNull(location.getCurrentTimeslot());

        // create a calendar period for today, with a timeslot at this moment
        List<Timeslot> activeCPInsideHours = TestSharedMethods.activeCalendarPeriodsInsideHours(location);
        timeslotDAO.addTimeslots(activeCPInsideHours);

        // now, the currentTimeslot should be initialized
        location = locationDao.getAllActiveLocations().get(0);
        Assert.assertNotNull(location.getCurrentTimeslot());
    }

}
