package blok2.daos;

import blok2.helpers.Pair;
import blok2.model.Authority;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TestDBCalendarPeriodDao extends TestDao {

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    private Location testLocation;


    // the reason for making this an attribute of the class
    // is to make sure the values are deleted when something
    // goes wrong
    private List<CalendarPeriod> calendarPeriods;
    private List<CalendarPeriod> updatedPeriods;
    private List<CalendarPeriod> pastPeriods;
    private List<CalendarPeriod> upcomingPeriods;
    private CalendarPeriod activePeriodsOutsideHours;
    private CalendarPeriod activePeriodsInsideHours;

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);
        testLocation = TestSharedMethods.testLocation(authority.clone());
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);
        updatedPeriods = TestSharedMethods.testCalendarPeriodsButUpdated(testLocation);
        pastPeriods = TestSharedMethods.pastCalendarPeriods(testLocation);
        upcomingPeriods = TestSharedMethods.upcomingCalendarPeriods(testLocation);
        activePeriodsOutsideHours = TestSharedMethods.activeCalendarPeriodsOutsideHours(testLocation);
        activePeriodsInsideHours = TestSharedMethods.activeCalendarPeriodsInsideHours(testLocation);

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @Test
    public void addCalendarPeriodsTest() throws SQLException {
        // Add calendar periods to database
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);

        // Check if the addition worked properly
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        calendarPeriods.sort(Comparator.comparing(CalendarPeriod::toString));

        Assert.assertEquals("addCalendarPeriodsTest", calendarPeriods, actualPeriods);
    }

    @Test
    public void getStatusTest() throws SQLException {
        // First, add only past calendar periods
        calendarPeriodDao.addCalendarPeriods(pastPeriods);

        Assert.assertEquals("StatusTest, only past calendar periods",
                "GESLOTEN.",
                calendarPeriodDao.getStatus(testLocation.getName())
        );

        // Second, add upcoming calendar periods. The past periods may remain, these do not affect status
        calendarPeriodDao.addCalendarPeriods(upcomingPeriods);

        List<Pair<LocalDateTime, LocalDateTime>> upcomingBeginAndEndDates = upcomingPeriods.stream()
                .map(CalendarPeriod::getBeginAndEndDate)
                .sorted(Comparator.comparing(Pair::getFirst))
                .collect(Collectors.toList());

        LocalDateTime first = upcomingBeginAndEndDates.get(0).getFirst();
        Assert.assertEquals("StatusTest, past and upcoming calendar periods",
                String.format("GESLOTEN. Opent op %s om %s.", first.toLocalDate(), first.toLocalTime()),
                calendarPeriodDao.getStatus(testLocation.getName())
        );

        // Third, add active periods.
        // There are two cases here: the current time is within or outside the opening hours.
        // These cases will be handled seperately

        // First: outside hours
        List<CalendarPeriod> outsideHours = new ArrayList<>();
        outsideHours.add(activePeriodsOutsideHours);
        calendarPeriodDao.addCalendarPeriods(outsideHours);

        Assert.assertEquals("StatusTest, active period, outside hours",
                String.format("GESLOTEN. Opent om %s.", activePeriodsOutsideHours.getOpeningTime()),
                calendarPeriodDao.getStatus(testLocation.getName())
        );

        // Before the case of active period inside the hours, remove outside hours
        calendarPeriodDao.deleteCalendarPeriods(outsideHours);

        // Second: inside hours
        List<CalendarPeriod> insideHours = new ArrayList<>();
        insideHours.add(activePeriodsInsideHours);
        calendarPeriodDao.addCalendarPeriods(insideHours);

        Assert.assertEquals("StatusTest, active period, inside hours",
                String.format("OPEN. Sluit om %s.", activePeriodsInsideHours.getClosingTime()),
                calendarPeriodDao.getStatus(testLocation.getName())
        );

    }

    @Test
    public void updateCalendarPeriodsTest() throws SQLException {
        // Add calendar periods to database
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);

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
        // Add calendar periods to database
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);

        // Delete the calendar periods from the database
        calendarPeriodDao.deleteCalendarPeriods(calendarPeriods);

        // are the periods deleted?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        Assert.assertEquals("deleteCalendarPeriodsTest", 0, actualPeriods.size());
    }
}
