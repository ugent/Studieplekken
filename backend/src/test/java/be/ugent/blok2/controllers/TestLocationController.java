package be.ugent.blok2.controllers;

import be.ugent.blok2.configuration.RestAPITestAdapter;
import be.ugent.blok2.configuration.SecurityConfig;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.reservables.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * This test file checks if the REST service handling the different location operations works properly
 * */
@SpringBootTest(classes = SecurityConfig.class)
@ActiveProfiles({"dummy","test"})
@AutoConfigureMockMvc
public class TestLocationController {

    private static final String BASE_URL = "/api/locations";
    private RestAPITestAdapter restAPITestAdapter;

    private String nonExistingName = "gibberish";
    private Calendar testCalendar;
    private Location TEST_LOCATION;

    @Autowired
    public TestLocationController(MockMvc mockMvc) {
        restAPITestAdapter = new RestAPITestAdapter(mockMvc);
        Collection<Day> calendars = new ArrayList<>();
        for (int i = 10; i < 20; i++) {
            Day d = new Day(new CustomDate(2020,4,i,0,0,0), new Time(10,0,0),new Time(18,0,0), new CustomDate(2020,4,i-1,0,0,0));
            calendars.add(d);
        }
        testCalendar = new Calendar(calendars);
        Map<Language, String> desc = new HashMap<>();
        desc.put(Language.DUTCH,"test1");
        desc.put(Language.ENGLISH,"test2");
        TEST_LOCATION = new Location("Test",
                "TestRoad 2 Belgium", 0, 0, "", desc, "");
    }

    public void addTestLocation() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL, TEST_LOCATION);
    }

    public void deleteTestLocation() throws Exception {
        restAPITestAdapter.deleteNoContent(BASE_URL + "/" + TEST_LOCATION.getName());
    }

    @Test
    public void testGetAllLocations() throws Exception {
        this.addTestLocation();

        Location[] locations = restAPITestAdapter.getOk(BASE_URL, Location[].class);
        assertNotNull(locations);

        boolean flag = false;
        int i = 0;
        while(flag == false && i < locations.length){
            if(locations[i].getName().equals(TEST_LOCATION.getName())) flag = true;
            i++;
        }

       assertEquals(true, flag);

        this.deleteTestLocation();
    }

    /*
        checks if a get request can be made for a specific location
     */
    @Test
    public void testGetLocation() throws Exception {
        this.addTestLocation();
        Location location = restAPITestAdapter.getOk(BASE_URL + "/" + TEST_LOCATION.getName(), Location.class);
        assertNotNull(location);
        assertEquals(location, TEST_LOCATION);
        this.deleteTestLocation();
    }

    // when a get request is sent for a location that doesnt exist the server should return an empty response
    @Test
    public void testGetNonExistingLocation() throws Exception {
        Location location = restAPITestAdapter.getNonExisting(BASE_URL + '/' + nonExistingName, Location.class);
        assertNull(location);
    }

    // checks if a location can be deleted
    @Test
    public void testDeleteLocation() throws Exception {
        this.addTestLocation();

        // delete the test location
        this.deleteTestLocation();
        Location location = restAPITestAdapter.getNonExisting(BASE_URL + "/" + TEST_LOCATION.getName(), Location.class);
        assertNull(location);
    }

    // checks if a non existing can be 'deleted' and returns the correct status
    @Test
    public void testDeleteNonExistingLocation() throws Exception {
        restAPITestAdapter.deleteNotFound(BASE_URL + '/' + nonExistingName);
    }

    // checks if an existing location can be changed
    @Test
    public void testChangeLocation() throws Exception {
        this.addTestLocation();
        Location testGetNewLocation = restAPITestAdapter.getOk(BASE_URL + "/" + TEST_LOCATION.getName(), Location.class);
        assertNotNull(testGetNewLocation);
        assertEquals(TEST_LOCATION, testGetNewLocation);

        // testGetNewLocation.setDescription("new description");
        restAPITestAdapter.put(BASE_URL+"/"+testGetNewLocation.getName(), testGetNewLocation);
        Location changedLocation = restAPITestAdapter.getOk(BASE_URL + "/" + testGetNewLocation.getName(), Location.class);
        // assertEquals(changedLocation.getDescription(), testGetNewLocation.getDescription());

        // checks if you can change the name of a location
        String oldName = testGetNewLocation.getName();
        testGetNewLocation.setName("new Name");
        restAPITestAdapter.put(BASE_URL+"/"+oldName, testGetNewLocation);
        Location locationWithNewName = restAPITestAdapter.getOk(BASE_URL + "/" + testGetNewLocation.getName(), Location.class);
        assertEquals(locationWithNewName, testGetNewLocation);

        // if we try to do a get request for the old location it should return an error 404 not found
        Location location = restAPITestAdapter.getNonExisting(BASE_URL + "/" + oldName, Location.class);
        restAPITestAdapter.deleteNoContent(BASE_URL + "/" + testGetNewLocation.getName());
    }

    // checks if a non existing location 'can' be changed and returns the correct status
    @Test
    public void testChangeNonExistingLocation() throws Exception {
        restAPITestAdapter.putNotFound(BASE_URL+'/'+ TEST_LOCATION.getName(), TEST_LOCATION);
    }

    // checks if a new location can be added using a post request
    @Test
    public void testAddLocation() throws Exception {
        this.addTestLocation();
        Location testGetNewLocation = restAPITestAdapter.getOk(BASE_URL + "/" + TEST_LOCATION.getName(), Location.class);
        assertNotNull(TEST_LOCATION);
        assertEquals(TEST_LOCATION, testGetNewLocation);
        this.deleteTestLocation();
    }

    @Test
    public void testAddCalendarDaysToExistingLocation() throws Exception {
        this.addTestLocation();

        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_LOCATION.getName(), testCalendar);

        this.deleteTestLocation();
    }

    @Test
    public void testAddCalendarDaysToNonExistingLocation() throws Exception {
        restAPITestAdapter.postBadRequest( BASE_URL + '/' + nonExistingName , testCalendar);
    }

    @Test
    public void testDeleteCalendarDaysToExistingLocation() throws Exception {
        this.addTestLocation();

        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_LOCATION.getName(), testCalendar);

        restAPITestAdapter.deleteNoContent( BASE_URL + '/' + TEST_LOCATION.getName() + '/' + "2020-04-10T00:00:00" + '/' + "2020-04-12T00:00:00");

        this.deleteTestLocation();
    }

    @Test
    public void testDeleteCalendarDaysToNonExistingLocation() throws Exception {
        restAPITestAdapter.deleteBadRequest( BASE_URL + '/' + nonExistingName + '/' + "2020-04-10" + '/' + "2020-04-12");
    }

    @Test
    public void testDeleteCalendarDaysWithFalseDates() throws Exception {
        this.addTestLocation();

        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_LOCATION.getName(), testCalendar);

        restAPITestAdapter.deleteBadRequest( BASE_URL + '/' + TEST_LOCATION.getName() + '/' + "1800-66T00:00:00" + '/' + "1800-04-12T00:00:00");

        this.deleteTestLocation();
    }

}
