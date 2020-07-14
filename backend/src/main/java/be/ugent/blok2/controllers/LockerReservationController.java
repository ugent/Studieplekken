package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.IAccountDao;
import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.daos.ILockerReservationDao;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.helpers.exceptions.NoUserLoggedInWithGivenSessionIdMappingException;
import be.ugent.blok2.model.users.Authority;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.reservables.Location;
import be.ugent.blok2.reservables.Locker;
import be.ugent.blok2.reservations.LockerReservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import be.ugent.blok2.helpers.date.CustomDate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

/**
 * This controller handles all requests related to lockerreservations.
 * Such as creating reservations, list of reservations, cancelling reservations, ...
 */
@RestController
@RequestMapping("api/locker/reservations")
public class LockerReservationController extends AController {

    private ILockerReservationDao iLockerReservationDao;
    private IAccountDao iAccountDao;
    private ILocationDao iLocationDao;

    public LockerReservationController(ILockerReservationDao iLockerReservationDao, IAccountDao iAccountDao,
                                       ILocationDao iLocationDao) {
        this.iLockerReservationDao = iLockerReservationDao;
        this.iAccountDao = iAccountDao;
        this.iLocationDao = iLocationDao;
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "View a list of all lockerreservations of a user by id")
    public ResponseEntity getAllLockerReservationsOfUser(@PathVariable("id") String idString, HttpServletRequest request) throws NoUserLoggedInWithGivenSessionIdMappingException {
        User u = getCurrentUser(request);
        if (!isTesting() && u.getAuthorities().contains(new Authority(Role.STUDENT)) && u.getAuthorities().size() == 1 && !idString.equals(u.getAugentID())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try{
            return new ResponseEntity<>(iLockerReservationDao.getAllLockerReservationsOfUser(idString), HttpStatus.OK);
        } catch (NoSuchUserException ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/userByName/{name}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "View a list of all lockerreservations of a user by name")
    public List<LockerReservation> getAllLockerReservationsOfUserByName(@PathVariable("name") String name) {
        return iLockerReservationDao.getAllLockerReservationsOfUserByName(name);
    }

    @GetMapping("/location/{name}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "View a list of all lockerreservations of a location")
    public List<LockerReservation> getAllLockerReservationsOfLocation(@PathVariable("name") String name) {
        return iLockerReservationDao.getAllLockerReservationsOfLocation(name);
    }

    @GetMapping("/location/{name}/ongoing")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "View a list of all ongoing lockerreservations of a location")
    public List<LockerReservation> getAllLockerReservationsOfLocationWithoutKeyBroughtBack(@PathVariable("name") String locationName) throws Exception {
        return iLockerReservationDao.getAllLockerReservationsOfLocationWithoutKeyBroughtBack(locationName);
    }

    @GetMapping("/location/{name}/numberInUse")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Get the number of all ongoing lockerreservations of a location")
    public int getNumberOfLockerReservationsWithoutKeyBroughtBack(@PathVariable("name") String locationName){
        return iLockerReservationDao.getNumberOfLockersInUseOfLocation(locationName);
    }

    @GetMapping("/{userId}/{lockerId}/{startDate}/{endDate}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get the lockerreservation of the given user within the given period")
    public ResponseEntity<LockerReservation> getLockerReservation(@PathVariable("userId") String idString, @PathVariable("lockerId") int lockerID, @PathVariable("startDate") String startDateString, @PathVariable("endDate") String endDateString, HttpServletRequest request) throws Exception {
        User u = getCurrentUser(request);
        // make sure a student can not view other users their lockerreservations
        if (!isTesting() && u.getAuthorities().contains(new Authority(Role.STUDENT)) && u.getAuthorities().size() == 1 && !idString.equals(u.getAugentID())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // TODO: change frontend, the startDate and endDate used to be part of the PK
        //return new ResponseEntity<>(iLockerReservationDao.getLockerReservation(idString, lockerID, CustomDate.parseString(startDateString), CustomDate.parseString(endDateString)), HttpStatus.OK);
        return new ResponseEntity<>(iLockerReservationDao.getLockerReservation(idString, lockerID), HttpStatus.OK);
    }

    @DeleteMapping("/{userID}/{lockerId}/{startDate}/{endDate}")
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "Delete the lockerreservation of the given user within the given period")
    public ResponseEntity<String> deleteLockerReservation(@PathVariable("userID") String idString, @PathVariable("lockerId") int lockerId, @PathVariable("startDate") String startDateString, @PathVariable("endDate") String endDateString, HttpServletRequest request) throws NoUserLoggedInWithGivenSessionIdMappingException {
        ObjectMapper mapper = new ObjectMapper();

        User u = getCurrentUser(request);
        // make sure a student can not delete other users their lockerreservations
        if (!isTesting() && u.getAuthorities().contains(new Authority(Role.STUDENT)) && u.getAuthorities().size() == 1 && !idString.equals(u.getAugentID())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            CustomDate startDate = CustomDate.parseString(startDateString);
            CustomDate endDate = CustomDate.parseString(endDateString);

            // TODO: change frontend, the startDate and endDate used to be part of the PK
            //LockerReservation reservation = iLockerReservationDao.getLockerReservation(idString, lockerId, startDate, endDate);
            LockerReservation reservation = iLockerReservationDao.getLockerReservation(idString, lockerId);

            //check if reservation exists
            if(reservation == null){
                return new ResponseEntity<>(mapper.writeValueAsString("Locker reservation does not exist"), HttpStatus.BAD_REQUEST);
            }

            //check if the user has the key of the locker
            if (reservation.getKeyPickupDate() != null && reservation.getKeyReturnedDate() == null) {
                return new ResponseEntity<>(mapper.writeValueAsString("You can't delete a locker reservation if the student still has the key of the locker"), HttpStatus.BAD_REQUEST);
            }
            iLockerReservationDao.deleteLockerReservation(idString, lockerId);
        }
        catch (IllegalArgumentException | JsonProcessingException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping()
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STUDENT', 'EMPLOYEE')")
    @ApiOperation(value = "Update the given lockerreservation")
    public void changeLockerReservation(@RequestBody LockerReservation lockerReservation){
        this.iLockerReservationDao.changeLockerReservation(lockerReservation);
    }

    @PostMapping("/{location}/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "Reserve a locker")
    public ResponseEntity<String> addLockerReservation(@PathVariable("location") String locationName, @PathVariable("id") String augentID){

        ObjectMapper mapper = new ObjectMapper();
        //get lockers of location
        try {
            Location location = iLocationDao.getLocation(locationName);
            if(location == null){
                return new ResponseEntity<>(mapper.writeValueAsString("Location does not exist"), HttpStatus.BAD_REQUEST);
            }
            Collection<Locker> lockers = location.getLockers();
            if (lockers != null) {

                //get ongoing reservations;
                Collection<LockerReservation>  ongoingLockerReservations = iLockerReservationDao.getAllLockerReservationsOfLocationWithoutKeyBroughtBack(locationName);

                Collection<Locker> inUseLockers = new ArrayList<>();

                if(ongoingLockerReservations != null){
                    for(LockerReservation res : ongoingLockerReservations){
                        inUseLockers.add(res.getLocker());
                    }
                }

                for (Locker locker : lockers) {

                    //check if locker is occupied
                    if (!inUseLockers.contains(locker)) {
                        User user = iAccountDao.getUserById(augentID);
                        if (user == null) {
                            return new ResponseEntity<>(mapper.writeValueAsString("No user with id = " + augentID), HttpStatus.NOT_FOUND);
                        }

                        //get all locker reservations of user to check if he has no locker already
                        Collection<LockerReservation> reservations = iLockerReservationDao.getAllLockerReservationsOfUser(augentID);
                        if (reservations != null) {
                            for (LockerReservation reservation : reservations) {
                                if (reservation.getKeyReturnedDate() == null) {

                                    //User still has key from other locker
                                    return new ResponseEntity<>(mapper.writeValueAsString("User still has key of other locker"), HttpStatus.CONFLICT);
                                }
                            }
                        }

                        Calendar tdy = Calendar.getInstance();
                        CustomDate today = new CustomDate(tdy.get(Calendar.YEAR), tdy.get(Calendar.MONTH)+1, tdy.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                        LockerReservation lockerReservation = new LockerReservation(locker, user, today, location.getEndPeriodLockers());
                        iLockerReservationDao.addLockerReservation(lockerReservation);
                        return new ResponseEntity<>(mapper.writeValueAsString("Successfully reserved locker"), HttpStatus.CREATED);
                    }
                }
                return new ResponseEntity<>(mapper.writeValueAsString("There are no lockers available right now"), HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(mapper.writeValueAsString("This location has no lockers"), HttpStatus.BAD_REQUEST);
        }
        catch(JsonProcessingException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(NoUserLoggedInWithGivenSessionIdMappingException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleUnauthorized() {

    }

    @ExceptionHandler({IllegalArgumentException.class, Exception.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handle() {

    }
}
