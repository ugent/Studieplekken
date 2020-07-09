package be.ugent.blok2.daos.dummies;

import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.daos.ILocationReservationDao;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.date.Time;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.DateFormatException;
import be.ugent.blok2.helpers.exceptions.NoSuchLocationException;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservables.Locker;
import be.ugent.blok2.reservations.LocationReservation;
import io.swagger.models.auth.In;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.DefaultEditorKit;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Profile("dummy")
@Service
public class DummyLocationDao extends ADummyDao implements ILocationDao {

    // a locations name is used as key for the Map
    private Map<String, Location> locations;
    private Random r = new Random();
    private static int lockerID = 0;
    private static Map<Language, String> descriptions = new HashMap<>();
    public static final Location TESTLOCATION = new Location("Kantienbergtest", "Stalhof 45, 9000 Gent",
            500, 5, "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d250" +
            "8.7176778614967!2d3.7258169160418304!3d51.039834879561006!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!" +
            "1s0x47c371570fc31a25%3A0xc1ef400b95e70679!2sUniversiteit%20Gent%20-%20Resto%20Kantienberg!5e0!3m2!1snl!2sb" +
            "e!4v1582414654027!5m2!1snl!2sbe", descriptions, "https://architectura.be/img-poster/0514-HART_e20.jpg");
    private ILocationReservationDao iLocationReservationDao;

    public DummyLocationDao(ILocationReservationDao iLocationReservationDao) {
        this.iLocationReservationDao=iLocationReservationDao;
        locations = new HashMap<>();

        descriptions.put(Language.DUTCH,"test1");
        descriptions.put(Language.ENGLISH,"test2");
        try {
            locations.put(TESTLOCATION.getName(), TESTLOCATION);
            Scanner scanner = new Scanner(new File("..\\Locations.csv"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] pieces = line.split(";");
                String imageUrl;
                try {
                    imageUrl = pieces[5];
                } catch (Exception e) {
                    imageUrl = "https://everestgloballtd.com/frontend/images/gallery/default-corporate-image.jpg";
                }
                if (pieces.length >= 4) {
                    int numberOfLockers = 0;
                    if (r.nextBoolean()) {
                        numberOfLockers = r.nextInt(150);
                    }

                    Map<Language, String> desc = new HashMap<>();
                    desc.put(Language.DUTCH,"test1");
                    desc.put(Language.ENGLISH,"test2");

                    Location location = new Location(pieces[0], pieces[3], Integer.parseInt(pieces[1]), numberOfLockers, "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3" +
                            "!1d2508.461346449728!2d3.725643616041962!3d51.04456867956177!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3" +
                            "m3!1m2!1s0x47c371505918a89f%3A0x215e897a80bba60d!2sStudentenhuis%20De%20Therminal!5e0!3m2!1snl!2sbe!4v" +
                            "1582414683281!5m2!1snl!2sbe", desc,
                            imageUrl);

                    /*Collection<Locker> lockers = new ArrayList<>();
                    for(int i=0; i<numberOfLockers; i++){
                        Locker locker = new Locker(i, location.getName(), 2);
                        locker.setId(countLockers);
                        lockers.add(locker);
                        countLockers++;
                    }*/

                    locations.put(location.getName(), location);
                    addLockers(location.getName(), numberOfLockers);
                    //location.setLockers(lockers);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File with locations not found");

            HashMap<Language, String> desc = new HashMap<>();
            desc.put(Language.DUTCH,"test1");
            desc.put(Language.ENGLISH,"test2");

            // standard test locations
            locations.put("Therminal", new Location("Therminal", "Hoveniersberg 24, 9000 Gent",
                    500, 150, "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3" +
                    "!1d2508.461346449728!2d3.725643616041962!3d51.04456867956177!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3" +
                    "m3!1m2!1s0x47c371505918a89f%3A0x215e897a80bba60d!2sStudentenhuis%20De%20Therminal!5e0!3m2!1snl!2sbe!4v" +
                    "1582414683281!5m2!1snl!2sbe", desc, "https://www.ugent.be/img/dcom/portretten/blok-at-therminal2012.jpg"));
            locations.put("Kantienberg", new Location("Kantienberg", "Stalhof 45, 9000 Gent",
                    500, 0, "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1" +
                    "d2508.7176778614967!2d3.7258169160418304!3d51.039834879561006!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!" +
                    "3m3!1m2!1s0x47c371570fc31a25%3A0xc1ef400b95e70679!2sUniversiteit%20Gent%20-%20Resto%20Kantienberg!5e0" +
                    "!3m2!1snl!2sbe!4v1582414654027!5m2!1snl!2sbe", desc, "https://architectura.be/img-po" +
                    "ster/0514-HART_e20.jpg"));
        }
    }

    @Override
    public List<Location> getAllLocations() {
        ArrayList<Location> allLocations = new ArrayList<>();
        for( Location l: locations.values() ){
            allLocations.add(l.locationWithoutScanners());
        }
        return allLocations;
    }

    @Override
    public List<Location> getAllLocationsWithoutLockersAndCalendar(){
        ArrayList<Location> allLocations = new ArrayList<>();
        for( Location l: locations.values() ){
            allLocations.add(l.locationWithoutScanners());
        }
        return allLocations;
    }

    @Override
    public List<Location> getAllLocationsWithoutLockers(){
        ArrayList<Location> allLocations = new ArrayList<>();
        for( Location l: locations.values() ){
            allLocations.add(l.locationWithoutScanners());
        }
        return allLocations;
    }

    @Override
    public List<Location> getAllLocationsWithoutCalendar(){
        ArrayList<Location> allLocations = new ArrayList<>();
        for( Location l: locations.values() ){
            allLocations.add(l.locationWithoutScanners());
        }
        return allLocations;
    }

    @Override
    public Location addLocation(Location location) throws AlreadyExistsException {
        System.out.println(locations);
        if(locations.containsKey(location.getName())){
            throw new AlreadyExistsException("A location with name " + location.getName() + " already exists. " +
                    "Use updateLocation() instead.");
        }
        locations.put(location.getName(), location);
        if (locations.containsKey(location.getName()) && locations.get(location.getName()).equals(location)) {
            return location;
        }
        return null;
    }

    @Override
    public Location getLocation(String locationName) {
        Location loc = locations.get(locationName);
        if (loc == null) throw new IllegalArgumentException();
        return loc;
    }

    @Override
    public Location getLocationWithoutCalendar(String locationName){
        Location loc = locations.get(locationName);
        if(loc == null) throw new IllegalArgumentException();
        return loc.locationWithoutScanners();
    }
    
    @Override
    public Location getLocationWithoutLockers(String locationName){
        Location loc = locations.get(locationName);
        if(loc == null) throw new IllegalArgumentException();
        return loc.locationWithoutScanners();
    }

    @Override
    public Location getLocationWithoutLockersAndCalendar(String name) {
        Location loc = locations.get(name);
        if(loc == null) throw new IllegalArgumentException();
        return loc.locationWithoutScanners();
    }

    @Override
    public void changeLocation(String locationName, Location location) {
        if(!locations.containsKey(locationName)){
            throw new NoSuchLocationException("No location with name " + locationName + " found.");
        }
        if(!locationName.equals(location.getName()) && locations.containsKey(location.getName())){
            throw new AlreadyExistsException("Location with name " + location.getName() + " already exists.");
        }
        locations.put(location.getName(), location);
        if (!locationName.equals(location.getName())) {
            // if the location's name has changed we add a new location entry under the new name and delete the old one
            locations.remove(locationName);
        }
    }

    @Override
    public void deleteLocation(String locationName) {
        if(!locations.containsKey(locationName)){
            throw new NoSuchLocationException("No location with name " + locationName + " found.");
        }
        locations.remove(locationName);
    }

    @Override
    public Collection<Locker> getLockers(String locationName) {
        return locations.get(locationName).getLockers();
    }

    @Override
    public void addLockers(String locationName, int count) {
        Location location = locations.get(locationName);
        int n = location.getNumberOfLockers();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                location.addLocker(n++, lockerID++);
            }
        } else if (count < 0) {
            deleteLockers(locationName, n + count);
        }
    }

    @Override
    public void deleteLockers(String locationName, int startNumber){
        locations.get(locationName).deleteLockers(startNumber);
    }

    @Override
    public void addCalendarDays(String locationName, Calendar calendar){
        if(!locations.containsKey(locationName)){
            throw new NoSuchLocationException("No location with name " + locationName + " found.");
        }
        for(Day newDay : calendar.getDays()){
            Collection<Day> days = locations.get(locationName).getCalendar();
            Day oldDay = null;
            for(Day d : days){
                if(d.getDate().toString().equals(newDay.getDate().toString())){
                    oldDay = d;
                }
            }
            if(oldDay != null){
                locations.get(locationName).getCalendar().remove(oldDay);
            }
            locations.get(locationName).getCalendar().add(newDay);
        }
    }

    @Override
    public void setScannersForLocation(String name, List<User> sc) {
        locations.get(name).setScanners(sc);
    }

    @Override
    public List<String> getScannersFromLocation(String name) {
        List<String> scanners = new ArrayList<>();
        for(User u: locations.get(name).getScanners()){
            scanners.add(u.shortString());
        }
        return scanners;
    }

    @Override
    public Map<String, Integer> getCountOfReservations(CustomDate d) {
        HashMap<String, Integer> count = new HashMap<>();
        for (Location l: locations.values()){
            List<LocationReservation> reservations = iLocationReservationDao.getAllLocationReservationsOfLocation(l.getName());
            reservations.removeIf(locationReservation -> !locationReservation.getDate().isSameDay(d));
            count.put(l.getName(),reservations.size());
        }
        return count;
    }

    @Override
    public void deleteCalendarDays(String locationName, String startdate, String enddate) throws DateFormatException {
        if(!locations.containsKey(locationName)){
            throw new NoSuchLocationException("No location with name " + locationName + " found.");
        }
            CustomDate startDate = CustomDate.parseString(startdate);
            CustomDate endDate = CustomDate.parseString(enddate);
            int start = startDate.getYear()*404 + startDate.getMonth()*31 + startDate.getDay();
            int end = endDate.getYear()*404 + endDate.getMonth()*31 + endDate.getDay();
            Collection<Day> days = locations.get(locationName).getCalendar();
            Collection<Day> markedDays = new ArrayList<>();
            for(Day d : days){
                int day = d.getDate().getYear()*404 + d.getDate().getMonth()*31 + d.getDate().getDay();
                if(start <= day && day <= end){
                    markedDays.add(d);
                }
            }
            for(Day d : markedDays){
                locations.get(locationName).getCalendar().remove(d);
            }

    }

    @Override
    public Collection<Day> getCalendarDays(String locationName) {
        return locations.get(locationName).getCalendar();
    }
}
