package be.ugent.blok2.controllers;

import be.ugent.blok2.configuration.RestAPITestAdapter;
import be.ugent.blok2.configuration.SecurityConfig;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservables.Locker;
import be.ugent.blok2.reservations.LocationReservation;
import be.ugent.blok2.reservations.LockerReservation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = SecurityConfig.class)
@ActiveProfiles({"dummy", "test"})
@AutoConfigureMockMvc
public class TestScalability {
    private RestAPITestAdapter restAPITestAdapter;
    private static final String LOCATION_URL = "/api/locations";
    private static final String ACCOUNT_URL = "/api/account";
    private static final String LOCATION_RESERVATION_URL = "/api/location/reservations";
    private static final String PENALTY_URL = "/api/penalties";
    private static final String LOCKER_RESERVATION_URL = "/api/locker/reservations";
    private static int REQUESTS = 100;

    private Locker TEST_LOCKER;
    private CustomDate START_DATE;
    private CustomDate END_DATE;
    private List<LocationReservation> TEST_LOCATION_RESERVATION = new ArrayList<>();

    private User TEST_USER = new User("000170058073", "Doe", "John"
            , "paulien.callebaut@ugent.be", "johnspassword", "UGent"
            , new Role[]{Role.STUDENT},25,"001700580731");

    private static Map<Language, String> descriptions = new HashMap<>();
    private Location TEST_LOCATION = new Location("testLocation",
            "TestRoad 2 Belgium", REQUESTS, 0, "", descriptions, "");

    @Autowired
    public TestScalability(MockMvc mockMvc) {
        restAPITestAdapter = new RestAPITestAdapter(mockMvc);

        TEST_LOCKER = new Locker(1, TEST_LOCATION.getName());
        Collection<Locker> ls = new ArrayList<>();
        ls.add(TEST_LOCKER);
        TEST_LOCATION.setLockers(ls);

        Calendar tdy = Calendar.getInstance();
        START_DATE = new CustomDate(tdy.get(Calendar.YEAR), tdy.get(Calendar.MONTH)+1, tdy.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        if(tdy.get(Calendar.MONTH) == Calendar.DECEMBER){
            END_DATE =  new CustomDate(tdy.get(Calendar.YEAR)+1, 0, 1, 0, 0, 0);
        } else {
            END_DATE = new CustomDate(tdy.get(Calendar.YEAR), tdy.get(Calendar.MONTH)+2, 1, 0, 0, 0);
        }

        TEST_LOCATION.setStartPeriodLockers(START_DATE);
        TEST_LOCATION.setEndPeriodLockers(END_DATE);

        LocalDate localDate = LocalDate.now();
        CustomDate customDate = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0, 0);
        Collection<Day> calendars = new ArrayList<>();
        Day today= new Day(customDate, new Time(10,0,0),new Time(18,0,0), customDate);
        calendars.add(today);
        TEST_LOCATION.setCalendar(calendars);

        TEST_LOCATION_RESERVATION.add(new LocationReservation(TEST_LOCATION, TEST_USER, customDate));
    }
    @BeforeEach
    public void addTestInstances() throws Exception {
        restAPITestAdapter.postCreated(LOCATION_URL, TEST_LOCATION);
        restAPITestAdapter.postCreated(ACCOUNT_URL+ "/new/by/employee", TEST_USER);
        restAPITestAdapter.postCreated(LOCATION_RESERVATION_URL, TEST_LOCATION_RESERVATION);
        restAPITestAdapter.postCreated(LOCKER_RESERVATION_URL + '/' + TEST_LOCKER.getLocation() + '/' + TEST_USER.getAugentID());
    }

    @AfterEach
    public void deleteTestInstances() throws Exception {
        restAPITestAdapter.deleteNoContent(LOCATION_RESERVATION_URL + '/' + TEST_LOCATION_RESERVATION.get(0).getUser().getAugentID() + '/' +
                TEST_LOCATION_RESERVATION.get(0).getDate().toString());
        restAPITestAdapter.deleteNoContent(LOCKER_RESERVATION_URL+"/"+TEST_USER.getAugentID()+"/" + TEST_LOCKER.getId() + '/' +START_DATE.toString() + '/' + END_DATE.toString());
        restAPITestAdapter.deleteNoContent(LOCATION_URL + '/' + TEST_LOCATION.getName());
        restAPITestAdapter.deleteNoContent(ACCOUNT_URL+ '/' + TEST_USER.getAugentID());
    }

    @Test
    public void testGetAllLocations() throws Exception {

        for (int i = 0; i < REQUESTS; i++) {
            Location[] locations = restAPITestAdapter.getOk(LOCATION_URL, Location[].class);
            assertNotNull(locations);
            assertTrue(locations.length >= 1);
        }

    }

    @Test
    public void testGetLocation() throws Exception {

        for (int i = 0; i < REQUESTS; i++) {
            Location location = restAPITestAdapter.getOk(LOCATION_URL + '/' + TEST_LOCATION.getName(), Location.class);
            assertNotNull(location);
            assertEquals(location, TEST_LOCATION);
        }

    }

    @Test
    public void testGetUserById() throws Exception {

        for (int i = 0; i < REQUESTS; i++) {
            User user = restAPITestAdapter.getOk(ACCOUNT_URL + "/" + TEST_USER.getAugentID(), User.class);
            assertEquals(TEST_USER.getAugentID(), user.getAugentID());
        }

    }

    @Test
    public void testGetLocationReservation() throws Exception {
        for (int i = 0; i < REQUESTS; i++) {
            List<LocationReservation> l = new ArrayList<>();
            LocationReservation locationReservation = restAPITestAdapter.getOk(LOCATION_RESERVATION_URL + "/user/" +
                    TEST_USER.getAugentID() + "/date/" +
                    TEST_LOCATION_RESERVATION.get(0).getDate().toString(), LocationReservation.class);
            Assertions.assertNotNull(locationReservation);
            Assertions.assertEquals(locationReservation, TEST_LOCATION_RESERVATION.get(0));
        }

    }

    @Test
    public void TestGetAllLockerReservationsOfLocation() throws Exception {
        for (int i = 0; i < REQUESTS; i++) {
            LockerReservation[] lockerReservations = restAPITestAdapter.getOk(LOCKER_RESERVATION_URL + "/location/" + TEST_LOCATION.getName(), LockerReservation[].class);
            Assertions.assertNotNull(lockerReservations);
            assertNotEquals(0, lockerReservations.length);
        }
    }

    @Test
    public void testGetPenaltyEvents() throws Exception {
        for (int i = 0; i < REQUESTS; i++) {
            PenaltyEvent[] penaltyEvents = restAPITestAdapter.getOk(PENALTY_URL, PenaltyEvent[].class);
            assertNotNull(penaltyEvents);
            assertNotEquals(penaltyEvents.length, 0);
        }
    }
}
