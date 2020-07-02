package be.ugent.blok2.controllers;

import be.ugent.blok2.configuration.RestAPITestAdapter;
import be.ugent.blok2.configuration.SecurityConfig;
import be.ugent.blok2.daos.dummies.DummyLocationReservationDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservations.LocationReservation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/*
 * This test file checks if the REST service handling the different locationreservations operations works properly
 * */
@SpringBootTest(classes = SecurityConfig.class)
@ActiveProfiles({"dummy", "test"})
@AutoConfigureMockMvc
public class TestLocationReservationController {

    private static final String BASE_URL = "/api/location/reservations";
    private static final String BASE_URL_LOCATION = "/api/locations";
    private static final String BASE_URL_ACCOUNT = "/api/account";

    private RestAPITestAdapter restAPITestAdapter;

    private static Map<Language, String> descriptions = new HashMap<>();
    private Location TEST_LOCATION = new Location("testLocation",
                                         "TestRoad 2 Belgium", 10, 0, "", descriptions, "");


    //email should be available in LDAP
    private User TEST_USER = new User("000170058073", "Doe", "John"
            , "paulien.callebaut@ugent.be", "johnspassword", "UGent"
            , new Role[]{Role.STUDENT},25,"001700580731");

    private List<LocationReservation> TEST_LOCATION_RESERVATION = new ArrayList<>();
    private List<LocationReservation> TEST_LOCATION_RESERVATION_TODAY= new ArrayList<>();

    @Autowired
    public TestLocationReservationController(MockMvc mockMvc) {
        restAPITestAdapter = new RestAPITestAdapter(mockMvc);
        TEST_LOCATION_RESERVATION.add(new LocationReservation(TEST_LOCATION, TEST_USER, new CustomDate(2020,4,13,0,0,0)));

        LocalDate localDate = LocalDate.now();
        CustomDate customDate = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0, 0);
        TEST_LOCATION_RESERVATION_TODAY.add(new LocationReservation(TEST_LOCATION, TEST_USER, customDate));
        Collection<Day> calendars = new ArrayList<>();
        for (int i = 10; i < 20; i++) {
            Day d = new Day(new CustomDate(2020,4,i,0,0,0), new Time(10,0,0),new Time(18,0,0), new CustomDate(2020,4,i,6,0,0));
            calendars.add(d);
        }
        Day today= new Day(customDate, new Time(10,0,0),new Time(18,0,0), customDate);
        calendars.add(today);
        TEST_LOCATION.setCalendar(calendars);
    }

    @BeforeEach
    public void addTestInstances() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL_ACCOUNT+ "/new/by/employee", TEST_USER);
        restAPITestAdapter.postCreated(BASE_URL_LOCATION, TEST_LOCATION);
        restAPITestAdapter.postCreated(BASE_URL, TEST_LOCATION_RESERVATION);
        restAPITestAdapter.postCreated(BASE_URL, TEST_LOCATION_RESERVATION_TODAY);
    }

    @AfterEach
    public void deleteTestInstances() throws Exception {
        restAPITestAdapter.deleteNoContent(BASE_URL + "/" +TEST_LOCATION_RESERVATION.get(0).getUser().getAugentID() + "/" +
                TEST_LOCATION_RESERVATION.get(0).getDate().toString());
        restAPITestAdapter.deleteNoContent(BASE_URL + "/" + TEST_LOCATION_RESERVATION_TODAY.get(0).getUser().getAugentID() + "/" +
                TEST_LOCATION_RESERVATION_TODAY.get(0).getDate().toString());
        restAPITestAdapter.deleteNoContent(BASE_URL_ACCOUNT+ '/' + TEST_USER.getAugentID());
        restAPITestAdapter.deleteNoContent(BASE_URL_LOCATION + "/" + TEST_LOCATION.getName());
    }

    @Test
    public void testGetAllLocationReservationsOfUser() throws Exception {
        LocationReservation[] locationReservations = restAPITestAdapter.getOk(BASE_URL + "/user/" +
                TEST_USER.getAugentID(), LocationReservation[].class);
        assertNotNull(locationReservations);
        assertNotEquals( 0, locationReservations.length);
    }

    @Test
    public void testGetAllLocationReservationsNonExistingUser() throws Exception {
        int id = -6325646;
        restAPITestAdapter.getNotFound(BASE_URL + "/user/" + id);
    }

    @Test
    public void testGetAllLocationReservationsByName() throws Exception {
        LocationReservation[] reservations = restAPITestAdapter.getOk(BASE_URL + "/user/" + TEST_USER.getAugentID(), LocationReservation[].class);
        assertNotEquals(0, reservations.length);
    }

    @Test
    public void testGetAllLocationReservationsOfLocation() throws Exception {
        LocationReservation[] locationReservations = restAPITestAdapter.getOk(BASE_URL + "/location/" +
                TEST_LOCATION.getName(), LocationReservation[].class);
        assertNotNull(locationReservations);
        assertNotEquals(0, locationReservations.length);
    }

    @Test
    public void testGetAllLocationReservationsOfNonExistingLocation() throws Exception {
        String name = "------------";
        LocationReservation[] locationReservations = restAPITestAdapter.getOk(BASE_URL + "/location/" + name, LocationReservation[].class);
        assertNotNull(locationReservations);
        assertEquals(0 , locationReservations.length);
    }

    @Test
    public void testGetLocationReservation() throws Exception {
        LocationReservation locationReservation = (restAPITestAdapter.getOk(BASE_URL + "/user/" +
                TEST_USER.getAugentID() + "/date/" +
                TEST_LOCATION_RESERVATION.get(0).getDate().toString(), LocationReservation.class));
        assertNotNull(locationReservation);
        assertEquals(locationReservation, TEST_LOCATION_RESERVATION.get(0));
    }

    @Test
    public void testGetNonExistingLocationReservation() throws Exception {
        int id = -6325646;
        restAPITestAdapter.getNotFound(BASE_URL + "/user/" + id + "/date/" + TEST_LOCATION_RESERVATION.get(0).getDate().toString());
    }


    @Test
    public void testDeleteNonExistingLocationReservation() throws Exception {
        int id = -6325646;
        restAPITestAdapter.deleteNotFound(BASE_URL + '/' + id + '/' + TEST_LOCATION_RESERVATION.get(0).getDate().toString());
    }

    @Test
    public void testAddExistingLocationReservation() throws Exception {
        restAPITestAdapter.postConflict(BASE_URL, TEST_LOCATION_RESERVATION);
    }

    @Test
    public void testGetMaxPenaltyPoints() throws Exception {
        Integer max = restAPITestAdapter.getOk(BASE_URL + "/maxPenaltyPoints", Integer.class);
        if (max < 0) {
            fail("Max penalty points is negative.");
        }
    }

    @Test
    public void testGetMaxCancelDate() throws Exception {
        CustomDate date = restAPITestAdapter.getOk(BASE_URL + "/maxCancelDate", CustomDate.class);
    }

    @Test
    public void testSetAllReservationsICE() throws Exception {
        LocalDate localDate = LocalDate.now();
        restAPITestAdapter.postOk(BASE_URL + "/closeICE/" + DummyLocationReservationDao.TEST_LOCATION.getName());
        LocationReservation[] reservations = restAPITestAdapter.getOk(BASE_URL + "/location/" + DummyLocationReservationDao.TEST_LOCATION.getName(), LocationReservation[].class);
        for (LocationReservation res : reservations) {
            // Only reservations of today will be set to attended.
            if (res.getDate().isSameDay(new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()))) {
                assertNotNull(res.getAttended());
                assertTrue(res.getAttended());
            }
        }
    }

    @Test
    public void testGetAbsentStudentsOfLocation() throws Exception {
        restAPITestAdapter.postOk(BASE_URL + "/closeICE/" + TEST_LOCATION.getName());
        LocationReservation[] reservations = restAPITestAdapter.getOk(BASE_URL + "/location/" + TEST_LOCATION.getName(), LocationReservation[].class);
        ArrayList<LocationReservation> reservationsList = new ArrayList<>(Arrays.asList(reservations));

        //after this operation, this list contains all absent students (on different dates)
        reservationsList.removeIf(locationReservation -> locationReservation.getAttended() != null && locationReservation.getAttended());

        for (LocationReservation res : reservationsList) {
            LocationReservation[] r = restAPITestAdapter.getOk(BASE_URL + "/absent/" + res.getLocation().getName() + "/" + res.getDate().toString(), LocationReservation[].class);
            List<LocationReservation> locationReservations = Arrays.asList(r);
            assertTrue(locationReservations.contains(res));
        }
    }


    @Test
    public void testCountReservedSeatsOfLocation() throws Exception {
        Map<String, Integer> count = restAPITestAdapter.getOk(BASE_URL + "/count/" +
                TEST_LOCATION_RESERVATION_TODAY.get(0).getDate(), Map.class);

        //there should be one test_reservation on this day for the test location
        assertEquals(count.get(TEST_LOCATION.getName()), 1);
    }

    @Test
    public void testCountReservedSeatsOfLocationNonExisting() throws Exception {
        CustomDate clone = TEST_LOCATION_RESERVATION.get(0).getDate().clone();
        clone.setYear(3000);
        Map<String, Integer> countMap = restAPITestAdapter.getOk(BASE_URL + "/count/"+clone, Map.class);

        //except when we're in the year 3000, no location should have a reserved seat
        for(Integer c: countMap.values()){
            assertEquals(0, c);
        }
    }

    @Test
    public void testSendEmailsAndAddPenaltyPoints() throws Exception {
        MultiValueMap<String,String > params = new LinkedMultiValueMap<>();
        List<String> mailAddresses = new ArrayList<>();

        // You can put your own email in the TEST_USER instance if you want to check the actual sending of the mail
        mailAddresses.add(TEST_USER.getMail());
        params.put("mails", mailAddresses);
        restAPITestAdapter.postParamsAccepted(BASE_URL+"/sendMails",params);
    }
}
