package blok2.daos;

import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class TestDBCalendarPeriodDao extends TestDao {

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    private Location testLocation;
    private List<CalendarPeriod> calendarPeriods;

    // the reason for making this an attribute of the class
    // is to make sure the values are deleted when something
    // goes wrong
    private List<CalendarPeriod> updatedPeriods;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.clone());
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);
        updatedPeriods = TestSharedMethods.testCalendarPeriodsButUpdated(testLocation);

        // Add test objects to database
        locationDao.addLocation(testLocation);
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);
    }

    @Test
    public void addCalendarPeriodsTest() throws SQLException {
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        calendarPeriods.sort(Comparator.comparing(CalendarPeriod::toString));

        Assert.assertEquals("addCalendarPeriodsTest", calendarPeriods, actualPeriods);
    }

    @Test
    public void updateCalendarPeriodsTest() throws SQLException {
        // update the periods
        calendarPeriodDao.updateCalendarPeriods(calendarPeriods, updatedPeriods);

        // check whether the periods are successfully updated
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        updatedPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        Assert.assertEquals("updateCalendarPeriodsTest", updatedPeriods, actualPeriods);
    }

    @Test
    public void deleteCalendarPeriodsTest() throws SQLException {
        calendarPeriodDao.deleteCalendarPeriods(calendarPeriods);

        // are the periods deleted?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        Assert.assertEquals("deleteCalendarPeriodsTest", 0, actualPeriods.size());
    }
}
