package be.ugent.blok2.daos.dummies;


import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.daos.ILockerReservationDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservables.Locker;
import be.ugent.blok2.reservations.LockerReservation;
import org.apache.commons.codec.language.Soundex;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Profile("dummy")
@Service
public class DummyLockerReservationDao extends ADummyDao implements ILockerReservationDao {
    public static LockerReservation TEST_LOCKERRESERVATION;
    public static List<User> TEST_USERS;
    public static CustomDate TEST_DATE;
    public static Location TEST_LOCATION;
    private List<LockerReservation> lockerReservations = new ArrayList<>();
    private Soundex soundex = new Soundex();

    private ILocationDao iLocationDao;
    private IAccountDao accountDao;


    public DummyLockerReservationDao(ILocationDao iLocationDao, IAccountDao accountDao) {
        this.iLocationDao = iLocationDao;
        this.accountDao = accountDao;
        User a = new User("00170315498", "Janssens", "Jan", "jj@UGent.be", "jan", "UGent",  new Role[]{Role.STUDENT},0,"");
        User b = new User("2", "Bloemhof", "Lore", "lb@HOGent.be", "lore", "HOGent", new Role[]{Role.STUDENT},0,"");
        User c = new User("3", "Bloemhof", "Lore", "lb@HOGent.be", "lore", "HOGent", new Role[]{Role.STUDENT},0,"");
        User d = new User("4", "Bloemhof", "Lore", "lb@HOGent.be", "lore", "HOGent", new Role[]{Role.STUDENT},0,"");

        HashMap<Language, String> descriptions = new HashMap<>();
        descriptions.put(Language.DUTCH,"test1");
        descriptions.put(Language.ENGLISH,"test2");

        TEST_LOCATION = new Location("Therminal", "Hoveniersberg 24, 9000 Gent", 500, 150, "test", descriptions, "<iframe src=\"" +
                "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2508.4611638148913!2d3.725643615956718!3d51.0" +
                "44572052257635!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c371505918a89f%3A0x215e897a80b" +
                "ba60d!2sStudentenhuis%20De%20Therminal!5e0!3m2!1snl!2sbe!4v1582295840769!5m2!1snl!2sbe\" width=\"60" +
                "0\" height=\"450\" frameborder=\"0\" style=\"border:0;\" allowfullscreen=\"\"></iframe>");
        Location z = new Location("Kantienberg", "Stalhof 45, 9000 Gent", 500, 0, "test", descriptions, "<iframe src=\"https:" +
                "//www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d2508.6346434273382!2d3.7255650159566316!3d51.04136835249036!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c371572e2314" +
                "59%3A0xaec2b5045d8ce4a4!2sKantienberg%2C%209000%20Gent!5e0!3m2!1snl!2sbe!4v1582297626156!5m2!1snl!2sbe\" width=\"600\" height=\"450\" frameborder=\"0\" style=\"border:0;\" allo" +
                "wfullscreen=\"\"></iframe>");

        Locker TEST_LOCKER = new Locker(0, TEST_LOCATION.getName());
        Locker n = new Locker(1, "z");

        TEST_USERS = new ArrayList<>();
        TEST_USERS.add(a);
        TEST_USERS.add(b);
        List<User> h = new ArrayList<>();
        h.add(c);
        h.add(d);

        TEST_DATE = new CustomDate(1970, 1, 1, 0, 0, 0);
        TEST_LOCKERRESERVATION = new LockerReservation(TEST_LOCKER, a, new CustomDate(1970, 1, 1, 0, 0, 0), new CustomDate(1970, 1, 2, 0, 0, 0));
        lockerReservations.add(TEST_LOCKERRESERVATION);
        lockerReservations.add(new LockerReservation(n, c, new CustomDate(1970, 1, 1, 0, 0, 0), new CustomDate(1970, 1, 2, 0, 0, 0)));

        LocalDate localDate = LocalDate.now();
        CustomDate date = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        CustomDate date2 = new CustomDate(localDate.getYear(), localDate.getMonthValue() + 1, localDate.getDayOfMonth());
        int i=0;
        /*
        for (User u : DummyAccountDao.usersForReservations) {
            // waarom heeft een locker een id en een number?
            Locker l = new Locker(i, 30*24*60*60, TEST_LOCATION.getName(),i++);
            lockerReservations.add(new LockerReservation(l, u, date, date2));
        }
        */
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfUser(String augentID) throws NoSuchUserException {
        List<LockerReservation> loc = new ArrayList<>();
        if(accountDao.getUserById(augentID) == null){
            throw new NoSuchUserException("No user with id = " + augentID);
        }
        for (LockerReservation lo : lockerReservations) {
                if (lo.getOwner().getAugentID().equals(augentID)) {
                    loc.add(lo);
                }
        }
        return loc;
    }

    // searches fonetically for all LockerReservations whose users full name, first name or last name sounds like given parameter name
    @Override
    public List<LockerReservation> getAllLockerReservationsOfUserByName(String name) {
        List<LockerReservation> res = new ArrayList<>();
        String s = soundex.encode(name);
        for(LockerReservation l: lockerReservations){
            String s2 = soundex.encode(l.getOwner().getFirstName() + l.getOwner().getLastName());
            if(s.equals(s2)){
                res.add(l);
            } else if(s.equals(soundex.encode(l.getOwner().getFirstName()))){
                res.add(l);
            } else if(s.equals(soundex.encode(l.getOwner().getLastName()))){
                res.add(l);
            }

        }
        return res;
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfLocation(String name) {
        List<LockerReservation> loc = new ArrayList<>();
        for (LockerReservation lo : lockerReservations) {
            if (lo.getLocker().getLocation().equalsIgnoreCase(name)) {
                loc.add(lo);
            }
        }
        return loc;
    }

    @Override
    public List<LockerReservation> getAllLockerReservationsOfLocationWithoutKeyBroughtBack(String locationName){
        List<LockerReservation> reservations = new ArrayList<>();
        for(LockerReservation reservation : lockerReservations){
            if (reservation.getLocker().getLocation().equalsIgnoreCase(locationName) && reservation.getKeyReturnedDate() != null) {
                reservations.add(reservation);
            }
        }
        return reservations;
    }

    @Override
    public int getNumberOfLockersInUseOfLocation(String locationName){
        int count = 0;
        for(LockerReservation reservation : lockerReservations){
            if(reservation.getLocker().getLocation().equalsIgnoreCase(locationName) && reservation.getKeyReturnedDate() != null) {
                count++;
            }
        }
        return count;
    }

    @Override
    public LockerReservation getLockerReservation(String augentID, int lockerID, CustomDate startDate, CustomDate endDate) {
        for (LockerReservation lo : lockerReservations) {
            if (lo.getOwner().getAugentID().equals(augentID) && lo.getLocker().getId() == lockerID) {
                return lo;
            }
        }
        throw new IllegalArgumentException();
        //return null;
    }

    @Override
    public void deleteLockerReservation(String augentID, int lockerID, CustomDate startDate, CustomDate endDate) {
        for (LockerReservation lo : lockerReservations) {
            if (lo.getOwner().getAugentID().equals(augentID) && lo.getLocker().getId() == lockerID) {
                lockerReservations.remove(lo);
                return;
            }
        }
    }

    @Override
    public void changeLockerReservation(LockerReservation lockerReservation){
        for(LockerReservation res : lockerReservations){
            if(res.getLocker().equals(lockerReservation.getLocker()) && res.getOwner().equals(lockerReservation.getOwner()) &&
                    !res.equals(lockerReservation)){
                res.setKeyPickupDate(lockerReservation.getKeyPickupDate());
                res.setKeyReturnedDate(lockerReservation.getKeyReturnedDate());
            }
        }
    }

    @Override
    public void addLockerReservation(LockerReservation lockerReservation) {
        if (!lockerReservations.contains(lockerReservation)) {
            lockerReservations.add(lockerReservation);
            return;
        }
        throw new IllegalArgumentException();
    }
}
