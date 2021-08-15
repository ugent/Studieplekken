package blok2;

import blok2.daos.IAuthorityDao;
import blok2.daos.ICalendarPeriodDao;
import blok2.daos.IUserDao;
import blok2.helpers.Institution;
import blok2.helpers.TimeException;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.junit.Assert;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSharedMethods {

    public static Location testLocation(Authority authority, Building building) {
        Location testLocation = new Location();
        testLocation.setName("TestLocation");
        testLocation.setNumberOfSeats(50);
        testLocation.setNumberOfLockers(15);
        testLocation.setImageUrl("https://example.com/image.jpg");
        testLocation.setAuthority(authority);
        testLocation.setBuilding(building);
        testLocation.setForGroup(false);
        return testLocation;
    }

    public static Location testLocation1Seat(Authority authority, Building building) {
        Location testLocation = new Location();
        testLocation.setName("TestLocation2");
        testLocation.setNumberOfSeats(1);
        testLocation.setNumberOfLockers(1);
        testLocation.setImageUrl("https://example.com/image.jpg");
        testLocation.setAuthority(authority);
        testLocation.setBuilding(building);
        testLocation.setForGroup(false);
        return testLocation;
    }

    public static Location testLocation2(Authority authority, Building building) {
        Location testLocation2 = new Location();
        testLocation2.setName("SecondTestLocation");
        testLocation2.setNumberOfSeats(100);
        testLocation2.setNumberOfLockers(10);
        testLocation2.setImageUrl("https://example.com/picture.png");
        testLocation2.setAuthority(authority);
        testLocation2.setBuilding(building);
        testLocation2.setForGroup(true);
        return testLocation2;
    }

    public static Location testLocation3(Authority authority, Building building) {
        Location testLocation3 = new Location();
        testLocation3.setName("ThirdTestLocation");
        testLocation3.setNumberOfSeats(25);
        testLocation3.setNumberOfLockers(5);
        testLocation3.setImageUrl("https://example.com/picture.png");
        testLocation3.setAuthority(authority);
        testLocation3.setBuilding(building);
        testLocation3.setForGroup(true);
        return testLocation3;
    }

    public static Building testBuilding() {
        Building testBuilding = new Building();
        testBuilding.setName("TestBuilding");
        testBuilding.setAddress("TestStreet 123");
        testBuilding.setInstitution(Institution.UGent);
        return testBuilding;
    }

    public static Building testBuildingHoGent() {
        Building testBuilding = new Building();
        testBuilding.setName("TestBuilding HoGent");
        testBuilding.setAddress("TestStreet 123, HoGent");
        testBuilding.setInstitution(Institution.HoGent);
        return testBuilding;
    }

    public static LocationTag testTag() {
        return new LocationTag(1,
                "Stille ruimte",
                "Silent space");
    }

    public static LocationTag testTag2() {
        return new LocationTag(2,
                "Geschikt voor vergaderingen",
                "Suitable for meetings");
    }

    public static LocationTag testTag3() {
        return new LocationTag(3,
                "Geschikt voor invaliden",
                "Suitable for the less-abled");
    }

    public static Authority insertTestAuthority(IAuthorityDao authorityDao) {
        return insertTestAuthority("Test Authority", "a test description", authorityDao);
    }

    public static Authority insertTestAuthority2(IAuthorityDao authorityDao) {
        return insertTestAuthority("Second Test Authority", "second test description", authorityDao);
    }

    public static Authority insertTestAuthority(String name, String description, IAuthorityDao authorityDao) {
        Authority authority = new Authority();
        authority.setAuthorityName(name);
        authority.setDescription(description);
        authority = authorityDao.addAuthority(authority);
        Authority dbAuthority = authorityDao.getAuthorityByAuthorityId(authority.getAuthorityId());
        Assert.assertEquals("insertTestAuthority: Failed to insert Test Authority", authority, dbAuthority);
        return authority;
    }

    public static User adminTestUser() {
        return adminTestUser("admin");
    }

    public static User adminTestUser(String s) {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("First");
        user.setMail(s + "@ugent.be");
        user.setPassword("first_password");
        user.setInstitution(Institution.UGent);
        user.setUserId(s);
        user.setAdmin(true);
        return user;
    }

    public static User studentTestUser() {
        return studentTestUser("002");
    }

    public static User studentTestUser(String id) {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("Second");
        user.setMail(id + "@ugent.be");
        user.setPassword("second_password");
        user.setInstitution(Institution.UGent);
        user.setUserId(id);
        user.setAdmin(false);
        return user;
    }

    public static User studentTestUserHoGent(String id) {
        User user = new User();
        user.setLastName("Added User");
        user.setFirstName("Third");
        user.setMail(id + "@hogent.be");
        user.setPassword("second_password");
        user.setInstitution(Institution.HoGent);
        user.setUserId(id);
        user.setAdmin(false);
        return user;
    }

    public static void addTestUsers(IUserDao userDao, User... users) {
        for (User u : users) {
            userDao.addUser(u);
            User r = userDao.getUserById(u.getUserId()); // retrieved user
            Assert.assertEquals("addTestUsers, setup test user failed", u, r);
        }
    }

    public static void removeTestUsers(IUserDao userDao, User... users) {
        for (User u : users) {
            userDao.deleteUser(u.getUserId());
            try {
                userDao.getUserById(u.getUserId());
                Assert.fail("cleanup test user should throw NoSuchDatabaseObjectException after deletion");
            } catch (NoSuchDatabaseObjectException e) {
                Assert.assertTrue(true);
            }
        }
    }

    public static List<CalendarPeriod> testCalendarPeriods(Location location) {
        List<CalendarPeriod> calendarPeriods = new ArrayList<>();

        LocalDate date = LocalDate.now();
        LocalTime time;

        for (int i = 0; i < 2; i++) {
            CalendarPeriod period = new CalendarPeriod();
            period.setLocation(location);

            date = LocalDate.of(date.getYear(), date.getMonth(), 2 + 10 * i);
            period.setStartsAt(date);

            date = LocalDate.of(date.getYear(), date.getMonth(), 4 + 10 * i);
            period.setEndsAt(date);

            time = LocalTime.of(9, 0);
            period.setOpeningTime(time);

            time = LocalTime.of(17, 0);
            period.setClosingTime(time);

            time = LocalTime.of(0, 0);
            date = LocalDate.of(date.getYear(), date.getMonth(), 1);
            period.setReservableFrom(LocalDateTime.of(date, time));

            period.setReservable(true);
            period.setTimeslotLength(30);

            period.setSeatCount(location.getNumberOfSeats());

            period.initializeLockedFrom();
            calendarPeriods.add(period);
        }

        return calendarPeriods;
    }

    /**
     * Create CalendarPeriod that is completely in the past
     *
     * @param location the location for which to create the period
     * @return a CalendarPeriod in the past
     */
    public static CalendarPeriod pastCalendarPeriods(Location location) {
        LocalDateTime now = LocalDateTime.now();

        CalendarPeriod period = new CalendarPeriod();
        period.setLocation(location);
        period.setLockedFrom(now.minusWeeks(3));

        period.setStartsAt(now.minusDays(2).toLocalDate());
        period.setEndsAt(now.minusDays(1).toLocalDate());
        period.setOpeningTime(LocalTime.of(9, 0));
        period.setClosingTime(LocalTime.of(17, 0));

        period.setReservableFrom(now.minusDays(3));

        return period;
    }

    /**
     * Create CalendarPeriod that is completely in the future
     *
     * @param location the location for which to create the period
     * @return a CalendarPeriod in the future
     */
    public static CalendarPeriod upcomingCalendarPeriods(Location location) {
        LocalDateTime now = LocalDateTime.now();

        CalendarPeriod period = new CalendarPeriod();
        period.setLocation(location);
        period.setLockedFrom(now.minusWeeks(3));

        LocalTime openingTime = LocalTime.of(9, 0);
        LocalTime closingTime = LocalTime.of(17, 0);

        period.setStartsAt(now.plusDays(1).toLocalDate());
        period.setEndsAt(now.plusDays(2).toLocalDate());
        period.setOpeningTime(openingTime);
        period.setClosingTime(closingTime);

        long timeslotSize = ChronoUnit.MINUTES.between(openingTime, closingTime);

        period.setReservable(true);
        period.setTimeslotLength((int) timeslotSize);
        period.setReservableFrom(now);

        return period;
    }

    /**
     * Create a CalendarPeriod that is active (today is between start and end date) but not during the active hours
     *
     * @param location the location for which to create the period
     * @return a CalendarPeriod that is active, but outside the hours
     */
    public static CalendarPeriod activeCalendarPeriodsOutsideHours(Location location) throws TimeException {
        LocalDateTime now = LocalDateTime.now();

        CalendarPeriod period = new CalendarPeriod();
        period.setLocation(location);
        period.setLockedFrom(now.minusWeeks(3));

        period.setStartsAt(now.minusDays(1).toLocalDate());
        period.setEndsAt(now.plusDays(1).toLocalDate());

        // "Minutes" is the smallest time we can use while minimizing issues with the time at which the test is run.
        if (LocalTime.now().isAfter(LocalTime.of(23, 59)) || LocalTime.now().isBefore(LocalTime.of(0, 1)))
            throw new TimeException("Impossible to create active calendar period at this time");

        period.setOpeningTime(now.plusMinutes(1).toLocalTime());
        period.setClosingTime(now.plusMinutes(1).toLocalTime());

        period.setReservableFrom(now);

        return period;
    }

    /**
     * Create a CalendarPeriod that is active (today is between start and end date) and during the active hours
     *
     * @param location the location for which to create the period
     * @return a CalendarPeriod that is active and within hours
     */
    public static CalendarPeriod activeCalendarPeriodsInsideHours(Location location) throws TimeException {
        LocalDateTime now = LocalDateTime.now();

        CalendarPeriod period = new CalendarPeriod();
        period.setLocation(location);
        period.setLockedFrom(now.minusWeeks(3));

        period.setStartsAt(now.minusDays(1).toLocalDate());
        period.setEndsAt(now.plusDays(1).toLocalDate());

        // "Minutes" is the smallest time we can use while minimizing issues with the time at which the test is run.
        if (LocalTime.now().isAfter(LocalTime.of(23, 59)) || LocalTime.now().isBefore(LocalTime.of(0, 1)))
            throw new TimeException("Impossible to create active calendar period at this time");

        period.setOpeningTime(now.minusMinutes(1).toLocalTime());
        period.setClosingTime(now.plusMinutes(1).toLocalTime());

        period.setReservableFrom(now);

        return period;
    }

    public static void addCalendarPeriods(ICalendarPeriodDao calendarPeriodDao, CalendarPeriod... periods) throws SQLException {
        calendarPeriodDao.addCalendarPeriods(Arrays.asList(periods));
    }

    public static List<CalendarPeriod> testCalendarPeriodsButUpdated(Location location) {
        List<CalendarPeriod> updatedPeriods = new ArrayList<>();
        for (CalendarPeriod calendarPeriod : testCalendarPeriods(location)) {
            updatedPeriods.add(calendarPeriod.clone());
        }

        for (int i = 0; i < updatedPeriods.size(); i++) {
            updatedPeriods.get(i).setStartsAt(LocalDate.of(1970, 1, i + 1));
            updatedPeriods.get(i).setEndsAt(LocalDate.of(1970, 1, i + 10));
            updatedPeriods.get(i).setOpeningTime(LocalTime.of(9, i));
            updatedPeriods.get(i).setClosingTime(LocalTime.of(17, i));
            updatedPeriods.get(i).setReservableFrom(LocalDateTime.of(1970, 1, i + 1, 9, 0));
        }

        return updatedPeriods;
    }

}
