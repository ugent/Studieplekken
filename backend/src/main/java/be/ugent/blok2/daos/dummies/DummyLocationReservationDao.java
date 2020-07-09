package be.ugent.blok2.daos.dummies;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.ILocationReservationDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.NoSuchReservationException;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservations.LocationReservation;
import org.apache.commons.codec.language.Soundex;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Profile("dummy")
@Service
public class DummyLocationReservationDao extends ADummyDao implements ILocationReservationDao {
    private List<LocationReservation> locationReservations = new ArrayList<>();
    public static LocationReservation TEST_LOCATION_RESERVATION;
    public static User TEST_STUDENT;
    public static Location TEST_LOCATION;
    public static Day TEST_DAY;
    private Soundex soundex = new Soundex();

    private IAccountDao accountDao;

    public DummyLocationReservationDao(IAccountDao accountDao) {
        Map<Language, String> desc= new HashMap<>();
        desc.put(Language.DUTCH,"test1");
        desc.put(Language.ENGLISH,"test2");

        TEST_STUDENT = new User("0000000006002", "Janssens", "Jan", "jj@UGent.be", "jan", "UGent", new Role[]{Role.STUDENT},0,"");
        User b = new User("2", "Bloemhof", "Lore", "lb@HOGent.be", "lore", "HOGent", new Role[]{Role.STUDENT},0,"");
        TEST_LOCATION = new Location("Therminal", "Hoveniersberg 24, 9000 Gent", 500, 150, "test", desc, "<iframe src=\"" +
                "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2508.4611638148913!2d3.725643615956718!3d51.0" +
                "44572052257635!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c371505918a89f%3A0x215e897a80b" +
                "ba60d!2sStudentenhuis%20De%20Therminal!5e0!3m2!1snl!2sbe!4v1582295840769!5m2!1snl!2sbe\" width=\"60" +
                "0\" height=\"450\" frameborder=\"0\" style=\"border:0;\" allowfullscreen=\"\"></iframe>");
        Location z = new Location("Kantienberg", "Stalhof 45, 9000 Gent", 500, 0, "test", desc, "<iframe src=\"https:" +
                "//www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2508.6346434273382!2d3.7255650159566316!3d51.04136835249036!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c371572e2314" +
                "59%3A0xaec2b5045d8ce4a4!2sKantienberg%2C%209000%20Gent!5e0!3m2!1snl!2sbe!4v1582297626156!5m2!1snl!2sbe\" width=\"600\" height=\"450\" frameborder=\"0\" style=\"border:0;\" allo" +
                "wfullscreen=\"\"></iframe>");
        LocationReservation m = new LocationReservation(TEST_LOCATION, b, new CustomDate(1970, 1, 1, 0, 0, 0));
        TEST_LOCATION_RESERVATION = new LocationReservation(TEST_LOCATION, TEST_STUDENT, new CustomDate(1971, 1, 1, 0, 0, 0));
        LocationReservation o = new LocationReservation(z, b, new CustomDate(1972, 1, 1, 0, 0, 0));
        LocationReservation p = new LocationReservation(z, TEST_STUDENT, new CustomDate(1973, 1, 1, 0, 0, 0));
        LocationReservation q = new LocationReservation(TEST_LOCATION, b, new CustomDate(2020, 3, 30, 0, 0, 0));
        LocationReservation[] reservations = new LocationReservation[]{m, TEST_LOCATION_RESERVATION, o, p, q};

        locationReservations.addAll(Arrays.asList(reservations));

        // dummy data, every user has a reservation for therminal today
        LocalDate localDate = LocalDate.now();
        CustomDate date = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
/*
        for (User u : DummyAccountDao.usersForReservations) {
            locationReservations.add(new LocationReservation(TEST_LOCATION, u, date));
        }

 */

        User l = new User("00170315498", "De Coninck", "ssander", "jj@UGent.be", "jan", "UGent", new Role[]{Role.STUDENT},0,"");
        User x = new User("00170315498", "De Coninck", "sandder", "jj@UGent.be", "jan", "UGent", new Role[]{Role.STUDENT},0,"");
       /* locationReservations.add(new LocationReservation(TEST_LOCATION, l, date));
        locationReservations.add(new LocationReservation(TEST_LOCATION, x, date));
        locationReservations.add(new LocationReservation(TEST_LOCATION, l, date));
        locationReservations.add(new LocationReservation(TEST_LOCATION, x, date));
        */
        Collection<Day> calendars = new ArrayList<>();
        for (int i = 10; i < 20; i++) {
            Day d = new Day(new CustomDate(2020,4,i,0,0,0), new Time(10,0,0),new Time(18,0,0), new CustomDate(2020,4,i,6,0,0));
            calendars.add(d);
        }
        TEST_LOCATION.setCalendar(calendars);
        z.setCalendar(calendars);

        TEST_DAY = new Day(new CustomDate(2020,12,5),new Time(0,0,0),new Time(0,0,0),new CustomDate(1975,12,2));
        TEST_LOCATION.getCalendar().add(TEST_DAY);

        this.accountDao = accountDao;
        //for(User u: DummyAccountDao.usersForReservations)
         //   locationReservations.add(new LocationReservation(TEST_LOCATION, u, date));
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfUser(String augentID) throws NoSuchUserException {
        List<LocationReservation> loc = new ArrayList<>();
        if(accountDao.getUserById(augentID) == null){
            throw new NoSuchUserException("No user with id = " + augentID);
        }
        for (LocationReservation res : locationReservations) {
            if (res.getUser().getAugentID().equals(augentID)) {
                loc.add(res);
            }
        }
        return loc;
    }

    // searches phonetically for users
    @Override
    public List<LocationReservation> getAllLocationReservationsOfUserByName(String userName) {
        List<LocationReservation> res = new ArrayList<>();
        String s1 = soundex.encode(userName.toLowerCase());
        for (LocationReservation l : locationReservations) {
            String s2 = soundex.encode(l.getUser().getFirstName() + " " + l.getUser().getLastName());
            if (s1.equals(s2)) {
                res.add(l);
            } else if (s1.equals(soundex.encode(l.getUser().getFirstName()))) {
                res.add(l);
            } else if (s1.equals(soundex.encode(l.getUser().getLastName()))) {
                res.add(l);
            }
        }
        return res;
    }

    @Override
    public List<LocationReservation> getAllLocationReservationsOfLocation(String name) {
        List<LocationReservation> loc = new ArrayList<>();
        for (LocationReservation res : locationReservations) {
            if (res.getLocation().getName().toLowerCase().equals(name.toLowerCase())) {
                loc.add(res);
            }
        }
        return loc;
    }

    @Override
    public LocationReservation getLocationReservation(String augentID, CustomDate date) throws NoSuchReservationException {
        for (LocationReservation res : locationReservations) {
            if (res.getUser().getAugentID().equals(augentID) && res.getDate().equals(date)) {
                return res;
            }
        }
        throw new NoSuchReservationException("User with id = " + augentID + " has no reservation on " + date.toString());
    }

    @Override
    public void deleteLocationReservation(String augentID, CustomDate date) throws NoSuchReservationException{
        boolean removed = false;
        for (LocationReservation res : locationReservations) {
            if (res.getUser().getAugentID().equals(augentID) && res.getDate().equals(date)) {
                removed = locationReservations.remove(res);
                return;
            }
        }
        if(!removed){
            throw new NoSuchReservationException("User with id = " + augentID + " had no reservation, nothing is deleted");
        }
    }

    @Override
    public void addLocationReservation(LocationReservation locationReservation) {
        if (!locationReservations.contains(locationReservation)) {
            locationReservations.add(locationReservation);
        }
        throw new AlreadyExistsException("Reservation already exists");
    }

    public LocationReservation scanStudent(String location, String barcode) {
        // decode barcodes

        for (LocationReservation res : locationReservations) {
            if (res.getUser().getBarcode().equals(barcode) && res.getDate().isToday() &&  ( res.getAttended() == null || !res.getAttended() )) {
                res.setAttended(true); // mark student as attended
                return res;
            }
        }

        for (LocationReservation res : locationReservations) {
            if (res.getUser().getAugentID().substring(1).equals(barcode.substring(0,barcode.length()-1)) && res.getDate().isToday() &&  ( res.getAttended() == null || !res.getAttended() )) {
                res.setAttended(true); // mark student as attended
                return res;
            }
        }

        for (LocationReservation res : locationReservations) {
            if (res.getUser().getAugentID().equals(barcode.substring(0,barcode.length()-1)) && res.getDate().isToday() &&  ( res.getAttended() == null || !res.getAttended() )) {
                res.setAttended(true); // mark student as attended
                return res;
            }
        }

        if(barcode.charAt(0)=='0'){
            for (LocationReservation res : locationReservations) {
                if (res.getUser().getAugentID().equals(barcode.substring(1)) && res.getDate().isToday() &&  ( res.getAttended() == null || !res.getAttended() )) {
                    res.setAttended(true); // mark student as attended
                    return res;
                }
            }
        }

        return null;
    }

    @Override
    public void setAllStudentsOfLocationToAttended(String locationName, CustomDate day) {
        for (LocationReservation locationReservation : locationReservations) {
            if (locationReservation.getLocation().getName().toLowerCase().equals(locationName.toLowerCase())) {
                if (locationReservation.getDate().isSameDay(day)) {
                    locationReservation.setAttended(true);
                }
            }
        }
    }

    public List<LocationReservation> getAllLocationReservationsOfLocationOnDate(String name, CustomDate date) {
        List<LocationReservation> loc = new ArrayList<>();
        for (LocationReservation res : locationReservations) {
            if (res.getLocation().getName().toLowerCase().equals(name.toLowerCase()) && res.getDate().equals(date)) {
                loc.add(res);
            }
        }
        return loc;
    }

    @Override
    public int countReservedSeatsOfLocationOnDate(String location, CustomDate date) {
        return getAllLocationReservationsOfLocationOnDate(location, date).size();
    }

    // returns  a list of all students that were absent for their reservation at location at given date
    @Override
    public List<LocationReservation> getAbsentStudents(String location, CustomDate date) {
        List<LocationReservation> res = new ArrayList<>();
        for (LocationReservation locationReservation : locationReservations) {
            if (locationReservation.getLocation().getName().toLowerCase().equals(location.toLowerCase())
                    && locationReservation.getDate().isSameDay(date)) {
                if (locationReservation.getAttended()==null) {
                    res.add(locationReservation);
                } else if (!locationReservation.getAttended()) {
                    res.add(locationReservation);
                }
            }
        }
        return res;
    }

    @Override
    public List<LocationReservation> getPresentStudents(String location, CustomDate date) {
        List<LocationReservation> res = new ArrayList<>();
        for (LocationReservation locationReservation : locationReservations) {
            if (locationReservation.getLocation().getName().toLowerCase().equals(location.toLowerCase())
                    && locationReservation.getDate().isSameDay(date) && locationReservation.getAttended() != null
            && locationReservation.getAttended()) {
                res.add(locationReservation);
            }
        }
        return res;
    }

    @Override
    public void setReservationToUnAttended(String augentId, CustomDate date) {
        for (LocationReservation l : locationReservations) {
            if (l.getUser().getAugentID().equals(augentId) && l.getDate().isSameDay(date)) {
                l.setAttended(false);
            }
        }
    }


}
