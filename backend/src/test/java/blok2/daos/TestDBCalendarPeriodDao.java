package blok2.daos;

import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.helpers.TimeException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TestDBCalendarPeriodDao extends TestDao {

    private Logger logger = LoggerFactory.getLogger(TestDBCalendarPeriodDao.class);

    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    private ILocationDao locationDao;

    @Autowired
    private IAuthorityDao authorityDao;

    @Autowired
    private IBuildingDao buildingDao;

    private Location testLocation;

    // the reason for making this an attribute of the class
    // is to make sure the values are deleted when something
    // goes wrong
    private List<CalendarPeriod> calendarPeriods;
    private List<CalendarPeriod> updatedPeriods;

    // DateTimeFormatter to format the next opening hour in a consistent manner
    private final DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);

        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);
        updatedPeriods = TestSharedMethods.testCalendarPeriodsButUpdated(testLocation);

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @Test
    public void addCalendarPeriodsTest() throws SQLException {
        // Add calendar periods to database
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);

        // Check if the addition worked properly
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::getId));
        calendarPeriods.sort(Comparator.comparing(CalendarPeriod::getId));

        Assert.assertEquals("addCalendarPeriodsTest", calendarPeriods, actualPeriods);
    }

    @Test
    public void getStatusTest() throws SQLException {
        // First, add only past calendar periods
        CalendarPeriod pastPeriod = TestSharedMethods.pastCalendarPeriods(testLocation);
        calendarPeriodDao.addCalendarPeriods(Collections.singletonList(pastPeriod));

        Assert.assertEquals("StatusTest, only past calendar periods",
                new Pair<>(LocationStatus.CLOSED, ""),
                calendarPeriodDao.getStatus(testLocation.getName())
        );

        // Second, add upcoming calendar periods. The past periods may remain, these do not affect status
        CalendarPeriod upcomingPeriod = TestSharedMethods.upcomingCalendarPeriods(testLocation);
        calendarPeriodDao.addCalendarPeriods(Collections.singletonList(upcomingPeriod));

        Pair<LocationStatus, String> expectedStatus = new Pair<>(LocationStatus.CLOSED_UPCOMING, upcomingPeriod.getStartsAt() + " " + upcomingPeriod.getOpeningTime());
        Pair<LocationStatus, String> retrievedStatus = calendarPeriodDao.getStatus(testLocation.getName());
        Assert.assertEquals("StatusTest, past and upcoming calendar periods",
                expectedStatus,
                retrievedStatus
        );

        // Third, add active periods.
        // There are two cases here: the current time is within or outside the opening hours.
        // These cases will be handled separately
        // But, they cannot be run when now().time <= '00:01' or now().time >= '23:59'

        try {
            CalendarPeriod activePeriodOutsideHours = TestSharedMethods.activeCalendarPeriodsOutsideHours(testLocation);
            CalendarPeriod activePeriodInsideHours = TestSharedMethods.activeCalendarPeriodsInsideHours(testLocation);

            // First: outside hours
            List<CalendarPeriod> outsideHours = new ArrayList<>();
            outsideHours.add(activePeriodOutsideHours);
            calendarPeriodDao.addCalendarPeriods(outsideHours);

            Pair<LocationStatus, String> expected = new Pair<>(
                    LocationStatus.CLOSED_ACTIVE,
                    LocalDateTime.of(LocalDate.now(), activePeriodOutsideHours.getOpeningTime()).format(outputFormat)
            );
            Pair<LocationStatus, String> actual = calendarPeriodDao.getStatus(testLocation.getName());
            Assert.assertEquals("StatusTest, active period, outside hours", expected, actual);

            // Before the case of active period inside the hours, remove outside hours
            for (CalendarPeriod cp : outsideHours)
                calendarPeriodDao.deleteCalendarPeriod(cp);

            // Second: inside hours
            List<CalendarPeriod> insideHours = new ArrayList<>();
            insideHours.add(activePeriodInsideHours);
            calendarPeriodDao.addCalendarPeriods(insideHours);

            expected = new Pair<>(
                    LocationStatus.OPEN,
                    LocalDateTime.of(LocalDate.now(), activePeriodInsideHours.getClosingTime()).format(this.outputFormat)
            );
            actual = calendarPeriodDao.getStatus(testLocation.getName());
            Assert.assertEquals("StatusTest, active period, inside hours",
                    expected,
                    actual
            );
        } catch (TimeException ignore) {
            logger.warn("This test was run at a timestamp " + LocalDateTime.now() + " at which some things cannot be tested.");
        }
    }

    @Test
    public void updateCalendarPeriodsTest() throws SQLException {
        // Add calendar periods to database
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);

        // Make sure that the calendar periods to be updated get the same ids as set by the db
        for (int i = 0; i < calendarPeriods.size(); i++) {
            updatedPeriods.get(i).setId(calendarPeriods.get(i).getId());
        }

        // update the periods
        calendarPeriodDao.updateCalendarPeriods(calendarPeriods, updatedPeriods);

        // check whether the periods are successfully updated
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        updatedPeriods.sort(Comparator.comparing(CalendarPeriod::toString));
        Assert.assertEquals("updateCalendarPeriodsTest", new HashSet<>(updatedPeriods), new HashSet<>(actualPeriods));
    }

    @Test
    public void deleteCalendarPeriodsTest() throws SQLException {
        // Add calendar periods to database
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);

        // Delete the calendar periods from the database
        for (CalendarPeriod calendarPeriod : calendarPeriods)
            calendarPeriodDao.deleteCalendarPeriod(calendarPeriod);

        // are the periods deleted?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getName());
        Assert.assertEquals("deleteCalendarPeriodsTest", 0, actualPeriods.size());
    }
}
