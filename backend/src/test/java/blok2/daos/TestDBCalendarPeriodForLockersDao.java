package blok2.daos;

import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.CalendarPeriodForLockers;
import blok2.model.reservables.Location;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class TestDBCalendarPeriodForLockersDao  extends TestDao {

    @Autowired
    private ICalendarPeriodForLockersDao calendarPeriodForLockersDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    private Location testLocation;
    private Building testBuilding;
    private List<CalendarPeriodForLockers> calendarPeriodsForLockers;

    // the reason for making this an attribute of the class
    // is to make sure the values are deleted when something
    // goes wrong
    private List<CalendarPeriodForLockers> updatedPeriodsForLockers;

    @Override
    public void populateDatabase() throws SQLException {
        // setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);

        testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());
        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        calendarPeriodsForLockers = TestSharedMethods.testCalendarPeriodsForLockers(testLocation);
        updatedPeriodsForLockers = TestSharedMethods.testCalendarPeriodsForLockersButUpdated(testLocation);

        // Add test objects to database
        locationDao.addLocation(testLocation);
        calendarPeriodForLockersDao.addCalendarPeriodsForLockers(calendarPeriodsForLockers);
    }

    //@Test
    public void addCalendarPeriodsForLockersTest() throws SQLException {
        List<CalendarPeriodForLockers> actualPeriods = calendarPeriodForLockersDao
                .getCalendarPeriodsForLockersOfLocation(testLocation.getLocationId());
        actualPeriods.sort(Comparator.comparing(CalendarPeriodForLockers::toString));
        calendarPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));

        Assert.assertEquals("addCalendarPeriodsForLockersTest", calendarPeriodsForLockers, actualPeriods);
    }

    //@Test
    public void updateCalendarPeriodsForLockersTest() throws SQLException {
        // update the periods
        calendarPeriodForLockersDao.updateCalendarPeriodsForLockers(calendarPeriodsForLockers, updatedPeriodsForLockers);

        // check whether the periods are successfully updated
        List<CalendarPeriodForLockers> actualPeriods = calendarPeriodForLockersDao
                .getCalendarPeriodsForLockersOfLocation(testLocation.getLocationId());
        actualPeriods.sort(Comparator.comparing(CalendarPeriodForLockers::toString));
        updatedPeriodsForLockers.sort(Comparator.comparing(CalendarPeriodForLockers::toString));
        Assert.assertEquals("updateCalendarPeriodsForLockersTest", updatedPeriodsForLockers, actualPeriods);
    }

    @Test
    public void deleteCalendarPeriodsForLockersTest() throws SQLException {
        calendarPeriodForLockersDao.deleteCalendarPeriodsForLockers(calendarPeriodsForLockers);

        // are the periods deleted?
        List<CalendarPeriodForLockers> actualPeriods = calendarPeriodForLockersDao
                .getCalendarPeriodsForLockersOfLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteCalendarPeriodsForLockersTest", 0, actualPeriods.size());
    }
}
