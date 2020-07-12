package be.ugent.blok2.controllers;

import be.ugent.blok2.configuration.RestAPITestAdapter;
import be.ugent.blok2.configuration.SecurityConfig;
import be.ugent.blok2.daos.dummies.DummyLockerReservationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservables.Locker;
import be.ugent.blok2.reservations.LockerReservation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/*
 * This test file checks if the REST service handling the different lockerreservations operations works properly
 * */
@SpringBootTest(classes = SecurityConfig.class)
@ActiveProfiles({"dummy","test"})
@AutoConfigureMockMvc
public class TestLockerReservationController {
    private static final String BASE_URL = "/api/locker/reservations";
    private static final String BASE_URL_LOCATION = "/api/locations";
    private static final String BASE_URL_ACCOUNT = "/api/account";

    private RestAPITestAdapter restAPITestAdapter;
    private Location TEST_LOCATION;
    private Locker TEST_LOCKER;
    private CustomDate START_DATE;
    private CustomDate END_DATE;


    //user should be in LDAP
    private User TEST_USER = new User("000170058073", "Doe", "John"
            , "paulien.callebaut@ugent.be", "john", "UGent"
            ,  new Role[]{Role.STUDENT},25,"001700580731");


    @Autowired
    public TestLockerReservationController(MockMvc mockMvc) {
        restAPITestAdapter = new RestAPITestAdapter(mockMvc);
        TEST_LOCATION = new Location();
        TEST_LOCATION.setName("TestLocation");
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

    }

    @BeforeEach
    public void addTestInstance() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL_LOCATION, TEST_LOCATION);
        restAPITestAdapter.postCreated(BASE_URL_ACCOUNT+ "/new/by/employee", TEST_USER);
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_LOCKER.getLocation() + '/' + TEST_USER.getAugentID());
    }

    @AfterEach
    public void deleteTestInstance() throws Exception {
        restAPITestAdapter.deleteNoContent(BASE_URL_LOCATION + "/" + TEST_LOCATION.getName());
        restAPITestAdapter.deleteNoContent(BASE_URL_ACCOUNT+ '/' + TEST_USER.getAugentID());
        restAPITestAdapter.deleteNoContent(BASE_URL+"/"+TEST_USER.getAugentID()+"/" + TEST_LOCKER.getId() + '/' +START_DATE.toString() + '/' + END_DATE.toString());
    }

    @BeforeClass
    public void setup(){
        DummyLockerReservationDao.TEST_USERS.add(SecurityConfig.admin);
    }

    @Test
    public void TestGetAllLockerReservationsOfUser() throws Exception {
        LockerReservation[] lockerReservations = restAPITestAdapter.getOk(BASE_URL+"/user/"+
                TEST_USER.getAugentID(), LockerReservation[].class);
        assertNotNull(lockerReservations);
        assertEquals(lockerReservations.length, 1);
    }

    @Test
    public void TestGetAllLockerReservationsOfNonExistingUser() throws Exception {
        int id = -6325646;
        restAPITestAdapter.getNotFound(BASE_URL+"/user/"+id);
    }

    @Test
    public void TestGetAllLockerReservationsOfLocation() throws Exception {
        LockerReservation[] lockerReservations = restAPITestAdapter.getOk(BASE_URL+"/location/"+TEST_LOCATION.getName(), LockerReservation[].class);
        assertNotNull(lockerReservations);
        assertEquals(1, lockerReservations.length);
    }

    @Test
    public void TestGetAllLockerReservationsOfNonExistingLocation() throws Exception {
        String name = "------------";
        LockerReservation[] lockerReservations = restAPITestAdapter.getOk(BASE_URL+"/location/"+name, LockerReservation[].class);
        assertNotNull(lockerReservations);
        assertEquals(lockerReservations.length, 0);
    }

    @Test
    public void TestGetLockerReservation() throws Exception {
        LockerReservation lockerReservation = restAPITestAdapter.getOk(BASE_URL +
                '/' + TEST_USER.getAugentID()+
                '/' + TEST_LOCKER.getId() +
                '/' + START_DATE +
                '/' + END_DATE, LockerReservation.class);
        assertNotNull(lockerReservation);
        assertEquals(lockerReservation.getOwner(), TEST_USER );
        assertEquals(lockerReservation.getLocker(), TEST_LOCKER);
    }

    @Test
    public void TestGetNonExistingLockerReservation() throws Exception {
        restAPITestAdapter.getNonExisting(BASE_URL +
                '/' + TEST_USER.getAugentID()+
                '/' + "6666666666666" +
                '/' + START_DATE +
                '/' + END_DATE, LockerReservation.class);
    }

    @Test
    public void TestDeleteNonExistingLockerReservation() throws Exception {
        restAPITestAdapter.deleteBadRequest(BASE_URL+"/"+"gibberish"+"/" + TEST_LOCKER.getId() + '/' +START_DATE.toString() + '/' + END_DATE.toString());
    }

    @Test
    public void TestAddBadLockerReservation() throws Exception {
        restAPITestAdapter.postConflict(BASE_URL + '/' + TEST_LOCKER.getLocation() + '/' + TEST_USER.getAugentID());
    }

}
