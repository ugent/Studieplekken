package blok2.daos;

import blok2.BaseTest;
import blok2.TestSharedMethods;
import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.helpers.TimeException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Timeslot;
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
import java.util.stream.Collectors;

public class TestDBCalendarPeriodDao extends BaseTest {

    private final Logger logger = LoggerFactory.getLogger(TestDBCalendarPeriodDao.class);

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
    private List<Pair<CalendarPeriod, List<Timeslot>>> calendarPeriods;

    // DateTimeFormatter to format the next opening hour in a consistent manner
    private final DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void populateDatabase() throws SQLException {
        // Setup test objects
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);

        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        calendarPeriods = TestSharedMethods.testCalendarPeriods(testLocation);

        // Add test objects to database
        locationDao.addLocation(testLocation);
    }

    @Test
    public void addCalendarPeriodsTest() throws SQLException {
        // Add calendar periods to database
        calendarPeriodDao.addCalendarPeriods(calendarPeriods.stream().map(Pair::getFirst).collect(Collectors.toList()));
        calendarPeriodDao.addTimeslots(calendarPeriods.stream().map(Pair::getSecond).flatMap(Collection::stream).collect(Collectors.toList()));

        // Check if the addition worked properly
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getLocationId());
        actualPeriods.sort(Comparator.comparing(CalendarPeriod::getId));
        List<CalendarPeriod> sorted = calendarPeriods.stream().map(Pair::getFirst).sorted(Comparator.comparing(CalendarPeriod::getId)).collect(Collectors.toList());

        Assert.assertEquals("addCalendarPeriodsTest", sorted, actualPeriods);


        for (CalendarPeriod p : sorted) {
            List<Timeslot> dbTimeslots = calendarPeriodDao.getTimeslotsByCalendarPeriod(p);
            List<Timeslot> toAddTimeslots = calendarPeriods.stream().filter(c -> c.getFirst().getId().equals(p.getId()))
                                            .flatMap(s -> s.getSecond().stream())
                                            .sorted(Comparator.comparingInt(Timeslot::getTimeslotSeqnr))
                                        .collect(Collectors.toList());

            Assert.assertEquals("addCalendarPeriodsTest", dbTimeslots, toAddTimeslots);
        }
    }

    @Test
    public void getStatusTest() throws SQLException {
        // First, add only past calendar periods
        Pair<CalendarPeriod, List<Timeslot>> pastPeriod = TestSharedMethods.pastCalendarPeriods(testLocation);
        TestSharedMethods.addPair(calendarPeriodDao, pastPeriod);

        Location location = locationDao.getLocationById(testLocation.getLocationId());
        Assert.assertEquals("StatusTest, only past calendar periods",
                new Pair<>(LocationStatus.CLOSED, ""),
                location.getStatus()
        );

        // Second, add upcoming calendar periods. The past periods may remain, these do not affect status
        Pair<CalendarPeriod, List<Timeslot>> upcomingPeriod = TestSharedMethods.upcomingCalendarPeriods(testLocation);
        TestSharedMethods.addPair(calendarPeriodDao, upcomingPeriod);

        Pair<LocationStatus, String> expectedStatus = new Pair<>(LocationStatus.CLOSED_UPCOMING, upcomingPeriod.getSecond().get(0).getStartDate().format(outputFormat));
        location = locationDao.getLocationById(testLocation.getLocationId());
        Assert.assertEquals("StatusTest, past and upcoming calendar periods",
                expectedStatus,
                location.getStatus()
        );

        // Third, add active periods.
        // There are two cases here: the current time is within or outside the opening hours.
        // These cases will be handled separately
        // But, they cannot be run when now().time <= '00:01' or now().time >= '23:59'

        try {
            Pair<CalendarPeriod, List<Timeslot>> activePeriodOutsideHours = TestSharedMethods.activeCalendarPeriodsOutsideHours(testLocation);
            Pair<CalendarPeriod, List<Timeslot>> activePeriodInsideHours = TestSharedMethods.activeCalendarPeriodsInsideHours(testLocation);

            // First: outside hours
            TestSharedMethods.addPair(calendarPeriodDao, activePeriodOutsideHours);

            Pair<LocationStatus, String> expected = new Pair<>(
                    LocationStatus.CLOSED_ACTIVE,
                    LocalDateTime.of(LocalDate.now(), activePeriodOutsideHours.getSecond().get(0).getStartDate().toLocalTime()).format(outputFormat)
            );
            location = locationDao.getLocationById(testLocation.getLocationId());
            Assert.assertEquals("StatusTest, active period, outside hours", expected, location.getStatus());

            // Before the case of active period inside the hours, remove outside hours
            calendarPeriodDao.deleteCalendarPeriod(activePeriodInsideHours.getFirst());

            // Second: inside hours
            TestSharedMethods.addPair(calendarPeriodDao, activePeriodInsideHours);
            location = locationDao.getLocationById(testLocation.getLocationId());

            expected = new Pair<>(
                    LocationStatus.OPEN,
                    LocalDateTime.of(LocalDate.now(), activePeriodInsideHours.getSecond().get(0).getEndTime()).format(this.outputFormat)
            );
            location = locationDao.getLocationById(testLocation.getLocationId());
            Assert.assertEquals("StatusTest, active period, inside hours",
                    expected,
                    location.getStatus()
            );
        } catch (TimeException ignore) {
            logger.warn("This test was run at a timestamp " + LocalDateTime.now() + " at which some things cannot be tested.");
        }
    }

    @Test
    public void deleteCalendarPeriodsTest() throws SQLException {
        // Add calendar periods to database
        for (Pair<CalendarPeriod, List<Timeslot>> calendarPeriodListPair : calendarPeriods) {
            TestSharedMethods.addPair(calendarPeriodDao, calendarPeriodListPair);
        }

        // Delete the calendar periods from the database
        for (Pair<CalendarPeriod, List<Timeslot>> p : calendarPeriods)
            calendarPeriodDao.deleteCalendarPeriod(p.getFirst());

        // are the periods deleted?
        List<CalendarPeriod> actualPeriods = calendarPeriodDao.getCalendarPeriodsOfLocation(testLocation.getLocationId());
        Assert.assertEquals("deleteCalendarPeriodsTest", 0, actualPeriods.size());
    }
}
