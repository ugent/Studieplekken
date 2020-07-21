package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.daos.ILockerReservationDao;
import be.ugent.blok2.helpers.date.Calendar;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.DateFormatException;
import be.ugent.blok2.helpers.exceptions.NoSuchLocationException;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservations.LockerReservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * This controller handles all requests related to locations.
 * Such as creating locations, list of locations, edit locations, ...
 */
@RestController
@RequestMapping("api/locations")
@Api(value="Location management system", description = "Operations pertaining to available locations")
public class LocationController {

    private final ILocationDao locationDao;
    private final ILockerReservationDao lockerReservationDao;
    private final IAccountDao accountDao;

    public LocationController(ILocationDao locationDao, ILockerReservationDao lockerReservationDao,  IAccountDao accountDao) {
        this.locationDao = locationDao;
        this.lockerReservationDao = lockerReservationDao;
        this.accountDao = accountDao;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "View a list of available locations")
    public List<Location> getAllLocations() {
        List<Location> ret = locationDao.getAllLocations();
        sort(ret);  // sort based on code, in ascending order
        return ret;
    }

    @GetMapping("/noLockersAndCalendar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Get all locations without calendar and lockers")
    public List<Location> getAllLocationsWithoutLockersAndCalendar(){
        List<Location> locations = locationDao.getAllLocations();
        sort(locations);
        return locations;
    }

    @GetMapping("/noLockers")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Get all locations without lockers")
    public List<Location> getAllLocationsWithoutLockers(){
        List<Location> locations = locationDao.getAllLocations();
        sort(locations);
        return locations;
    }

    @GetMapping("/noCalendar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Get all locations without calendar")
    public List<Location> getAllLocationsWithoutCalendar(){
        List<Location> locations = locationDao.getAllLocations();
        sort(locations);
        return locations;
    }

    @GetMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "View a specific location")
    public Location getLocation(@PathVariable("name") String name) {
        return locationDao.getLocation(name);
    }

    @GetMapping("/{name}/noCalendar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Get location without calendar")
    public Location getLocationWithoutCalendar(@PathVariable("name") String name){
        return locationDao.getLocation(name);
    }

    @GetMapping("/{name}/noLockers")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Get location without lockers")
    public Location getLocationWithoutLockers(@PathVariable("name") String name){
        return locationDao.getLocation(name);
    }

    @GetMapping("/{name}/noLockersAndCalendar")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Get location without lockers and calendar")
    public Location getLocationWithoutLockersAndCalendar(@PathVariable("name") String name){
        return locationDao.getLocation(name);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Delete a location")
    public ResponseEntity deleteLocation(@PathVariable("name") String name) {
        try {
            locationDao.deleteLocation(name);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (NoSuchLocationException e){
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/calendar/{locationName}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Get the calendar days for the specified location")
    public Collection<Day> getCalendarDays(@PathVariable("locationName") String locationName) {
        return locationDao.getCalendarDays(locationName);
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Change a location")
    public ResponseEntity changeLocation(@PathVariable("name") String name, @RequestBody Location location) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Location previousLocation = locationDao.getLocation(name);
            int previousLockers = previousLocation.getNumberOfLockers();

            if(previousLockers > location.getNumberOfLockers()){
                //You can only remove lockers when there are no lockers in use in this location

                Collection<LockerReservation> ongoingReservations = lockerReservationDao.getAllLockerReservationsOfLocationWithoutKeyBroughtBack(name);
                if(ongoingReservations == null || ongoingReservations.size() == 0){

                    //delete lockers
                    locationDao.changeLocation(name, location);
                    locationDao.deleteLockers(location.getName(), location.getNumberOfLockers());
                }
                else{
                    return new ResponseEntity<>(mapper.writeValueAsString("Unable to delete lockers if there are lockers in use"), HttpStatus.BAD_REQUEST);
                }
            }
            else if(previousLockers <= location.getNumberOfLockers()){
                //lockers toevoegen
                locationDao.changeLocation(name, location);
                if(previousLockers != location.getNumberOfLockers()){
                    locationDao.addLockers(location.getName(), location.getNumberOfLockers()-previousLockers);
                }
            }
            return new ResponseEntity(HttpStatus.OK);
        }  catch (NoSuchLocationException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch(JsonProcessingException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }  catch (AlreadyExistsException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "Add a new location")
    public ResponseEntity addLocation(@RequestBody Location location) {
        try {
            Location addedLocation = locationDao.addLocation(location);
            return new ResponseEntity(addedLocation, HttpStatus.CREATED);
        } catch (AlreadyExistsException e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{name}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ApiOperation(value = "add calendar days to a location")
    public ResponseEntity addCalendarDays(@PathVariable("name") String name, @RequestBody Calendar calendar){
        try{
            locationDao.addCalendarDays(name, calendar);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (NoSuchLocationException e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{name}/{startdate}/{enddate}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "delete a calendar day from a location")
    public ResponseEntity deleteCalendarDay(@PathVariable("name") String name, @PathVariable("startdate") String startdate, @PathVariable("enddate") String enddate)  {
        try {
            locationDao.deleteCalendarDays(name, startdate, enddate);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (NoSuchLocationException | DateFormatException e){
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @GetMapping("/scanners/{name}")
    @ApiOperation(value = "get users that are allowed to scan at the given location")
    public ResponseEntity getScanners(@PathVariable("name") String name){
        return new ResponseEntity(locationDao.getScannersFromLocation(name), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/scanners/{name}")
    @ApiOperation(value = "give users the permissions to scan at the given location")
    public ResponseEntity postScanners(@PathVariable("name") String name, @RequestBody String[] scanners){
        List<User> sc = new ArrayList<>();
        for(String s: scanners){
            accountDao.setScannerLocation(s.substring(0, s.indexOf(" ")), name );
            sc.add(accountDao.getUserById(s.substring(0, s.indexOf(" "))));
        }

        locationDao.setScannersForLocation(name, sc);

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handle() {

    }

    private void sort(List<Location> list) {
        list.sort(Comparator.comparing(Location::getName));
    }
}

