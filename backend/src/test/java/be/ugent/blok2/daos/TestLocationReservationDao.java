package be.ugent.blok2.daos;

import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.NoSuchReservationException;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservations.LocationReservation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Test class mainly for testing different locationReservation operations such as making a reservation
// scanning in for a reservation and getting all student of a reservationday that haven't attended
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"dummy", "test"})
public class TestLocationReservationDao {

    @Autowired
    private ILocationReservationDao locationReservationDao;

    private Location location;
    private CustomDate date;
    private List<User> users;

    @Before
    public void setUp() {
        Map<Language, String> desc = new HashMap<>();
        desc.put(Language.DUTCH,"test1");
        desc.put(Language.ENGLISH,"test2");
        location = new Location("testLocationForTestLocationReservationDao",
                "", 5, 0, "", desc, "");
        LocalDate localDate = LocalDate.now();
        date = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        users = new ArrayList<>();

        // dummy users for testing purposes
        User user = new User("00000001", "", "testUser", "", "", "", new Role[]{Role.STUDENT},"");
        users.add(user);
        user = new User("00000002", "", "testUser", "", "", "", new Role[]{Role.STUDENT},"");
        users.add(user);
        user = new User("00000003", "", "testUser", "", "", "", new Role[]{Role.STUDENT},"");
        users.add(user);
    }

    @After
    public void cleanUp(){
        // cleanup
        for(User u: users){
            try {
                locationReservationDao.deleteLocationReservation(u.getAugentID(), date);
            } catch (NoSuchReservationException e){

            }
        }
    }

    @Test
    public void testScanStudent() {
        LocationReservation locationReservation = new LocationReservation(location, users.get(0), date);
        locationReservationDao.addLocationReservation(locationReservation);

        // attended is initialized to null
        assertNull(locationReservation.getAttended());

        // should scan correctly as the reservation is set for today
        locationReservationDao.scanStudent(location.getName(),users.get(0).getBarcode());

        LocationReservation l = locationReservationDao.getLocationReservation(users.get(0).getAugentID(), date);
        assertNotNull(l);
        assertTrue(l.getAttended());
    }

    @Test
    public void testGetAbsentStudents(){

        // add a reservation for all students in users list
        for(User u: users){
            LocationReservation locationReservation = new LocationReservation(location, u, date);
            locationReservationDao.addLocationReservation(locationReservation);
        }

        // only scan the first student
        locationReservationDao.scanStudent(location.getName(), users.get(0).getBarcode());

        List<LocationReservation> absentStudents = locationReservationDao.getAbsentStudents(location.getName(),date);

        // only 2 students haven't scanned yet
        assertEquals(absentStudents.size(), 2);

        // first student should not be in this list
        for(LocationReservation l: absentStudents){
            assertNotEquals(l.getUser(), users.get(0));
        }

    }

    @Test
    public void testSetAllStudentsToAttended(){

        // add a reservation for all students in users list
        for(User u: users){
            LocationReservation locationReservation = new LocationReservation(location, u, date);
            locationReservationDao.addLocationReservation(locationReservation);
        }

        // no students scanned

        locationReservationDao.setAllStudentsOfLocationToAttended(location.getName(), date);

        // check if all reservations have been set to attended

        // only reservations made for this location, should all be set to attended
        List<LocationReservation> locationReservations = locationReservationDao.getAllLocationReservationsOfLocation(location.getName());
        for(LocationReservation l: locationReservations){
            if(l.getAttended()!=null){
                assertTrue(l.getAttended());
            }else{
                fail();
            }
        }
    }
}
