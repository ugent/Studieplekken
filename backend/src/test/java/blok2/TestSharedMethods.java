package blok2;

import blok2.daos.IAccountDao;
import blok2.daos.IAuthorityDao;
import blok2.daos.ICalendarPeriodDao;
import blok2.helpers.Institution;
import blok2.helpers.Pair;
import blok2.helpers.TimeException;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.LocationTag;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.CalendarPeriodForLockers;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.users.User;
import org.bouncycastle.util.Times;
import org.junit.Assert;
import org.threeten.extra.YearWeek;

import java.sql.SQLException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public static Authority insertTestAuthority(IAuthorityDao authorityDao) throws SQLException {
        return insertTestAuthority("Test Authority", "a test description", authorityDao);
    }

    public static Authority insertTestAuthority2(IAuthorityDao authorityDao) throws SQLException {
        return insertTestAuthority("Second Test Authority", "second test description", authorityDao);
    }

    public static Authority insertTestAuthority(String name, String description, IAuthorityDao authorityDao) throws SQLException {
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
        user.setMail(s+"@ugent.be");
        user.setPassword("first_password");
        user.setInstitution(Institution.UGent);
        user.setAugentID(s);
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
        user.setAugentID(id);
        user.setAdmin(false);
        return user;
    }

    public static User authorityHolderTestUser(String id) {
        User user = new User();
        user.setLastName("AutorityMan");
        user.setFirstName("Authority");
        user.setMail(id + "@ugent.be");
        user.setPassword("second_password");
        user.setInstitution(Institution.UGent);
        user.setAugentID(id);
        user.setAdmin(false);
        return user;
    }

    public static void addTestUsers(IAccountDao accountDao, User... users) throws SQLException {
        for (User u : users) {
            accountDao.directlyAddUser(u);
            User r = accountDao.getUserById(u.getAugentID()); // retrieved user
            Assert.assertEquals("addTestUsers, setup test user failed", u, r);
        }
    }

    public static void removeTestUsers(IAccountDao accountDao, User... users) throws SQLException {
        for (User u : users) {
            accountDao.deleteUser(u.getAugentID());
            User r = accountDao.getUserById(u.getAugentID());
            Assert.assertNull("removeTestUsers, cleanup test user failed", r);
        }
    }

    public static List<Pair<CalendarPeriod, List<Timeslot>>> testCalendarPeriods(Location location) {
        List<Pair<CalendarPeriod, List<Timeslot>>> calendarPeriods = new ArrayList<>();

        YearWeek date = YearWeek.now();

        for (int i = -1; i < 1; i++) {
            LocalDateTime reservableFrom = LocalDateTime.now().withDayOfMonth(1);

            CalendarPeriod period = new CalendarPeriod(null, date.getYear(), date.getWeek(), null, 0, reservableFrom, false, location);
            period.setLocation(location);

            LocalTime mondayStartTime = LocalTime.of(8,0);
            LocalTime mondayEndTime = LocalTime.of(16,30);
            Timeslot timeslotMonday = new Timeslot(period, 0, DayOfWeek.MONDAY.getValue(), mondayStartTime, mondayEndTime, true, 10, 0);


            LocalTime fridayStartTime = LocalTime.of(12,0);
            LocalTime fridayEndTime = LocalTime.of(20, 0);
            Timeslot timeslotFriday = new Timeslot(period, 1, DayOfWeek.FRIDAY.getValue(), fridayStartTime, fridayEndTime, false, 10, 0);


            calendarPeriods.add(new Pair<>(period, Arrays.asList(timeslotMonday, timeslotFriday)));
        }

        return calendarPeriods;
    }

    /**
     * Create CalendarPeriod that is completely in the past
     * @param location the location for which to create the period
     * @return a CalendarPeriod in the past
     */
    public static Pair<CalendarPeriod, List<Timeslot>> pastCalendarPeriods(Location location) {
        YearWeek past = YearWeek.now().minusWeeks(1);
        LocalDateTime now = LocalDateTime.now();


        CalendarPeriod calendarPeriod = new CalendarPeriod(null, past.getYear(), past.getWeek(), null, 0, now.minusDays(3), false, location);

        LocalTime startTime = now.toLocalTime().plusMinutes(1);
        LocalTime endTime = now.toLocalTime().plusMinutes(2);
        Timeslot timeslot = new Timeslot(calendarPeriod, 0, now.getDayOfWeek().getValue(), startTime, endTime, true, 10, 0);

        return new Pair<>(calendarPeriod, Collections.singletonList(timeslot));
    }

    /**
     * Create CalendarPeriod that is completely in the future
     * @param location the location for which to create the period
     * @return a CalendarPeriod in the future
     */
    public static Pair<CalendarPeriod, List<Timeslot>> upcomingCalendarPeriods(Location location) {
        YearWeek past = YearWeek.now().plusWeeks(3);
        LocalDateTime now = LocalDateTime.now();

        CalendarPeriod calendarPeriod = new CalendarPeriod(null, past.getYear(), past.getWeek(), null, 0, now.minusDays(3), false, location);

        LocalTime startTime = now.toLocalTime().plusMinutes(1);
        LocalTime endTime = now.toLocalTime().plusMinutes(2);
        Timeslot timeslot = new Timeslot(calendarPeriod, 0, now.getDayOfWeek().getValue(), startTime, endTime, true, 10, 0);

        return new Pair<>(calendarPeriod, Collections.singletonList(timeslot));

    }

    /**
     * Create a CalendarPeriod that is active (today is between start and end date) but not during the active hours
     * @param location the location for which to create the period
     * @return a CalendarPeriod that is active, but outside the hours
     */
    public static Pair<CalendarPeriod, List<Timeslot>> activeCalendarPeriodsOutsideHours(Location location) throws TimeException {
        LocalDateTime now = LocalDateTime.now();

        YearWeek past = YearWeek.now();

        CalendarPeriod period = new CalendarPeriod(null, past.getYear(), past.getWeek(), null, 0, now.minusDays(3), false, location);

        if (LocalTime.now().isAfter(LocalTime.of(23, 59)) || LocalTime.now().isBefore(LocalTime.of(0, 1)))
            throw new TimeException("Impossible to create active calendar period at this time");


        LocalTime startTime = now.toLocalTime().plusMinutes(1);
        LocalTime endTime = now.toLocalTime().plusMinutes(2);
        Timeslot timeslot = new Timeslot(period, 0, now.getDayOfWeek().getValue(), startTime, endTime, true, 10, 0);



        return new Pair<>(period, Collections.singletonList(timeslot));
    }

    /**
     * Create a CalendarPeriod that is active (today is between start and end date) and during the active hours
     * @param location the location for which to create the period
     * @return a CalendarPeriod that is active and within hours
     */
    public static Pair<CalendarPeriod, List<Timeslot>> activeCalendarPeriodsInsideHours(Location location) throws TimeException {

        LocalDateTime now = LocalDateTime.now();

        YearWeek past = YearWeek.now();

        CalendarPeriod period = new CalendarPeriod(null, past.getYear(), past.getWeek(), null, 0, now.minusDays(3), false, location);

        if (LocalTime.now().isAfter(LocalTime.of(23, 59)) || LocalTime.now().isBefore(LocalTime.of(0, 1)))
            throw new TimeException("Impossible to create active calendar period at this time");


        LocalTime startTime = now.toLocalTime().minusMinutes(1);
        LocalTime endTime = now.toLocalTime().plusMinutes(1);
        Timeslot timeslot = new Timeslot(period, 0, now.getDayOfWeek().getValue(), startTime, endTime, true, 10, 0);



        return new Pair<>(period, Collections.singletonList(timeslot));
    }

    public static void addCalendarPeriods(ICalendarPeriodDao calendarPeriodDao, CalendarPeriod... periods) throws SQLException {
        calendarPeriodDao.addCalendarPeriods(Arrays.asList(periods));
    }


    public static List<CalendarPeriodForLockers> testCalendarPeriodsForLockers(Location location) {
        List<CalendarPeriodForLockers> calendarPeriods = new ArrayList<>();

        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        for (int i = 0; i < 2; i++) {
            CalendarPeriodForLockers period = new CalendarPeriodForLockers();
            period.setLocation(location);

            date = LocalDate.of(date.getYear(), date.getMonth(), 2 + 10*i);
            period.setStartsAt(date);

            date = LocalDate.of(date.getYear(), date.getMonth(), 4 + 10*i);
            period.setEndsAt(date);

            date = LocalDate.of(date.getYear(), date.getMonth(), 1);
            period.setReservableFrom(LocalDateTime.of(date, time));

            calendarPeriods.add(period);
        }

        return calendarPeriods;
    }

    public static List<CalendarPeriodForLockers> testCalendarPeriodsForLockersButUpdated(Location location) {
        List<CalendarPeriodForLockers> updatedPeriods = new ArrayList<>();
        for (CalendarPeriodForLockers calendarPeriod : testCalendarPeriodsForLockers(location)) {
            updatedPeriods.add(calendarPeriod.clone());
        }

        for (int i = 0; i < updatedPeriods.size(); i++) {
            updatedPeriods.get(i).setStartsAt(LocalDate.of(1970,1,i+1));
            updatedPeriods.get(i).setEndsAt(LocalDate.of(1970,1,i + 10));
            updatedPeriods.get(i).setReservableFrom(LocalDateTime.of(1970,1,i+1,9,0));
        }

        return updatedPeriods;
    }

    public static Pair<CalendarPeriod, List<Timeslot>>  addPair(ICalendarPeriodDao calendarPeriodDao, Pair<CalendarPeriod, List<Timeslot>> pair) throws SQLException {
        List<CalendarPeriod> p = calendarPeriodDao.addCalendarPeriods(Collections.singletonList(pair.getFirst()));
        List<Timeslot> tslist = calendarPeriodDao.addTimeslots(pair.getSecond());
        return new Pair<>(p.get(0), tslist);
    }
}
