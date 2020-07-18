package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.ILocationDao;
import be.ugent.blok2.daos.ILocationReservationDao;
import be.ugent.blok2.helpers.LocationReservationResponse;
import be.ugent.blok2.helpers.Resources;
import be.ugent.blok2.helpers.Variables;
import be.ugent.blok2.daos.IPenaltyEventsDao;
import be.ugent.blok2.helpers.exceptions.AlreadyExistsException;
import be.ugent.blok2.helpers.exceptions.NoSuchReservationException;
import be.ugent.blok2.helpers.exceptions.NoSuchUserException;
import be.ugent.blok2.helpers.exceptions.NoUserLoggedInWithGivenSessionIdMappingException;
import be.ugent.blok2.model.penalty.Penalty;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import be.ugent.blok2.model.users.Authority;
import be.ugent.blok2.helpers.date.Day;
import be.ugent.blok2.helpers.EmailService;
import be.ugent.blok2.model.users.Role;
import be.ugent.blok2.model.users.User;
import be.ugent.blok2.model.reservables.Location;
import be.ugent.blok2.model.reservations.LocationReservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import be.ugent.blok2.helpers.date.CustomDate;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

/**
 * This controller handles all requests related to locationreservations.
 * Such as creating reservations, list of reservations, cancelling reservations,
 * scanning of users, ...
 */
@RestController
@RequestMapping("api/location/reservations")
public class LocationReservationController extends AController {

    private ILocationReservationDao iLocationReservationDao;
    private EmailService emailService;
    private ResourceBundle applicationBundle;
    private IPenaltyEventsDao penaltyEventsDao;
    private ILocationDao iLocationDao;

    // used for broadcasting scanned locationReservations
    @Autowired
    private SimpMessagingTemplate template;

    public LocationReservationController(ILocationReservationDao iLocationReservationDao, EmailService emailService,
                                         IPenaltyEventsDao penaltyEventsDao, ILocationDao iLocationDao) {
        this.iLocationReservationDao = iLocationReservationDao;
        this.emailService = emailService;
        this.applicationBundle = Resources.applicationProperties;
        this.penaltyEventsDao = penaltyEventsDao;
        this.iLocationDao = iLocationDao;
    }

    @GetMapping("/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "View a list of all locationreservations of a user by id")
    public ResponseEntity getAllLocationReservationsOfUser(@PathVariable("id") String idString, HttpServletRequest request) throws NoUserLoggedInWithGivenSessionIdMappingException {
        int i = 0;
        User u = getCurrentUser(request);
        if (!isTesting() && u.getAuthorities().contains(new Authority(Role.STUDENT)) && u.getAuthorities().size() == 1 && !idString.equals(u.getAugentID())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try{
            List<LocationReservation> reservations = iLocationReservationDao.getAllLocationReservationsOfUser(idString);
            return new ResponseEntity<>(reservations, HttpStatus.OK);
        } catch (NoSuchUserException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/userByName/{userName}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "View a list of all locationreservations of a user by name")
    public List<LocationReservation> getAllLocationReservationsOfUserByName(@PathVariable("userName") String userName) {
        return iLocationReservationDao.getAllLocationReservationsOfUserByName(userName);
    }

    @GetMapping("/location/{name}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "View a list of all locationreservations of a location")
    public List<LocationReservation> getAllLocationReservationsOfLocation(@PathVariable("name") String name) {
        return iLocationReservationDao.getAllLocationReservationsOfLocation(name);
    }

    @GetMapping("/user/{id}/date/{date}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get the locationreservation of the user with the given id on the given date")
    public ResponseEntity getLocationReservation(@PathVariable("id") String idString, @PathVariable("date") String dateString, HttpServletRequest request) throws Exception {
        User u = getCurrentUser(request);
        // make sure the user is allowed to edit this locationreservation
        if (!isTesting() && u.getAuthorities().contains(new Authority(Role.STUDENT)) && u.getAuthorities().size() == 1 && !idString.equals(u.getAugentID())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        CustomDate date = CustomDate.parseString(dateString);
        try{
            LocationReservation locationReservation = iLocationReservationDao.getLocationReservation(idString, date);
            return new ResponseEntity<>(locationReservation, HttpStatus.OK);
        } catch (NoSuchReservationException ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/{id}/{date}")
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "Delete the locationreservation of the user with the given id on the given date")
    public ResponseEntity deleteLocationReservation(@PathVariable("id") String idString, @PathVariable("date") String dateString, HttpServletRequest request) throws Exception {
        User u = getCurrentUser(request);
        // make sure the user is allowed to edit this locationreservation
        if (!isTesting() && u.getAuthorities().contains(new Authority(Role.STUDENT)) && u.getAuthorities().size() == 1 && !idString.equals(u.getAugentID())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //define parameters for calculation of possible penalty points
        Calendar today = Calendar.getInstance();
        CustomDate timestamp = new CustomDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH),
                today.get(Calendar.HOUR_OF_DAY), today.get(Calendar.MINUTE), today.get(Calendar.SECOND));
        int maxPoints = penaltyEventsDao.getPenaltyEvent(PenaltyEvent.CODE_LATE_CANCEL).getPoints();
        int points = Penalty.calculateLateCancelPoints(CustomDate.parseString(dateString), maxPoints);

        //add penalty points if those are needed
        if(points > 0){
            Penalty p = new Penalty(idString, PenaltyEvent.CODE_LATE_CANCEL, timestamp, CustomDate.parseString(dateString), "", points);
            penaltyEventsDao.addPenalty(p);
        }
        CustomDate date = CustomDate.parseString(dateString);
        date.setHrs(0);
        date.setMin(0);
        try{
            iLocationReservationDao.deleteLocationReservation(idString, date);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchReservationException ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('STUDENT','EMPLOYEE')")
    @ApiOperation(value = "Reserve a location for the given user on the multiple given dates")
    public ResponseEntity<LocationReservationResponse> addLocationReservations(@RequestBody List<LocationReservation> locationReservations) {
	System.out.println("Hier kom ik met lijst l.size() = " + locationReservations.size() + " en l.getUser() = " + locationReservations.get(0).getUser());
        List<CustomDate> valid = new ArrayList<>();
        List<CustomDate> full = new ArrayList<>();

        try {
            Location loc = locationReservations.get(0).getLocation();

            Collection<Day> calendar = iLocationDao.getLocationWithoutLockers(loc.getName()).getCalendar();

            for (LocationReservation locationReservation : locationReservations) {
                CustomDate date = locationReservation.getDate();

                //Check if the location is open for this date
                Day day = null;
                if(calendar != null){
                    for (Day d : calendar) {
                        if (d.getDate().toString().equals(date.toString())) {
                            day = d;
                        }
                    }
                }

                if (day != null) {
                    //if day is not null, it means that the location is open for this day
                    //checking if the day for which someone is trying to reserve, is already open for reservation

                    CustomDate d = day.getOpenForReservationDate();

                    //calculate difference between current date and open for reservation date of this day
                    Calendar today = Calendar.getInstance();
                    int date_int = d.getYear() * 404 + d.getMonth() * 31 + d.getDay();
                    int today_int = today.get(Calendar.YEAR) * 404 + (today.get(Calendar.MONTH) + 1) * 31 + today.get(Calendar.DATE);
                    int datetime_int = d.getHrs() * 60 + d.getMin();
                    int todaytime_int = today.get(Calendar.HOUR_OF_DAY) * 60 + today.get(Calendar.MINUTE);

                    if (today_int > date_int || (today_int == date_int && todaytime_int >= datetime_int)) {
                        //day is open for reservation

                        //check if location is full for this date
                        int reservedSeats = iLocationReservationDao.countReservedSeatsOfLocationOnDate(locationReservation.getLocation().getName(), locationReservation.getDate());
                        if (reservedSeats >= locationReservation.getLocation().getNumberOfSeats()) {

                            //Location is full on this date
                            full.add(locationReservation.getDate());
                        }

                        //Check if user doesn't have to many penalty points, users can get penalty points by not showing up for a reservation.
                        //If you reach a certain amount of penalty points, you are not allowed to make a reservation.
                        else if (locationReservation.getUser().getPenaltyPoints() >= getMaxPenaltyPoints()) {
                            return new ResponseEntity<>(HttpStatus.CONFLICT);
                        }

                        else {
                            try{
                                iLocationReservationDao.addLocationReservation(locationReservation);
                                valid.add(locationReservation.getDate());
                            } catch (AlreadyExistsException ex){
                                //user has other reservations for this date, the user is only allowed to reserve for 1 location per day
                                return new ResponseEntity<>(HttpStatus.CONFLICT);
                            }
                        }
                    }
                }
            }
            LocationReservationResponse response = new LocationReservationResponse(valid, full);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/maxPenaltyPoints")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "When a user has more than this amount of penalty points, he is no longer able to reserve a location")
    public int getMaxPenaltyPoints() {
        return Variables.thresholdPenaltyPoints;
    }

    @GetMapping("/maxCancelDate")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "A user that cancels after this date, will receive penalty points")
    public CustomDate getMaxCancelDate() {
        return Variables.maxCancelDate;
    }

    @PostMapping("/scan/{location}/{barcode}")
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @ApiOperation(value = "This method is used to scan students at a location")
    public ResponseEntity<String> scanStudent(@PathVariable("location") String location, @PathVariable("barcode") String barcode) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // scanStudent checks if the student has a reservation for the current day of this location and returns that reservation if so
            LocationReservation locationReservation = iLocationReservationDao.scanStudent(location, barcode);
            if (locationReservation != null && locationReservation.getLocation().getName().toLowerCase().equals(location.toLowerCase())) {

                // this will send a message to a messagebroker where it will be broadcast to all subscribed clients (the scan employees)
                template.convertAndSend("/reservationScans/" + locationReservation.getLocation().getName(), locationReservation);
                System.out.println(template.getUserDestinationPrefix());

                return new ResponseEntity<>(mapper.writeValueAsString("Student has been marked as attending."), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(mapper.writeValueAsString("Student hasn't made a reservation for this location."), HttpStatus.BAD_REQUEST);
            }
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cancelScan/{location}/{augentId}")
    @ApiOperation(value = "This method is used to cancel a reservation")
    public ResponseEntity cancelScanOfStudent(@PathVariable("location") String location, @PathVariable("augentId") String augentId, HttpServletRequest request) throws NoUserLoggedInWithGivenSessionIdMappingException {
        User u = getCurrentUser(request);
        // make sure the user is allowed to edit this locationreservation
        if (!isTesting() && u.getAuthorities().contains(new Authority(Role.STUDENT)) && u.getAuthorities().size() == 1 && !augentId.equals(u.getAugentID())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalDate localDate = LocalDate.now();
        CustomDate customDate = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0, 0);
        iLocationReservationDao.setReservationToUnAttended(augentId, customDate);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // If there are any problems during the scanning process that resulted in no students being scanned
    // this method can be used to set all reservations of that location that day to attended so no students will get
    // undeserved penalty points
    @PostMapping("/closeICE/{locationName}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @ApiOperation(value = "In case of emergency, when something goes wrong during scanning, all students wil be set to attended for the given location")
    public void setAllReservationsICE(@PathVariable("locationName") String locationName) {
        LocalDate localDate = LocalDate.now();
        iLocationReservationDao.setAllStudentsOfLocationToAttended(locationName,
                new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()));
    }

    @GetMapping("/absent/{locationName}/{date}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @ApiOperation(value = "Gives a list of all users that where absent on the given location on the given date")
    public List<LocationReservation> getAbsentStudentsOfLocation(@PathVariable("locationName") String location,
                                                                 @PathVariable("date") String date) {
        return iLocationReservationDao.getAbsentStudents(location, CustomDate.parseString(date));
    }

    @GetMapping("/present/{locationName}/{date}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @ApiOperation(value = "Gives a list of all users that where present on the given location on the given date")
    public List<LocationReservation> getPresentStudentsOfLocation(@PathVariable("locationName") String location,
                                                                  @PathVariable("date") String date) {
        return iLocationReservationDao.getPresentStudents(location, CustomDate.parseString(date));
    }

    @GetMapping("/count/{date}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN','STUDENT','EMPLOYEE')")
    @ApiOperation(value = "Gives the number of reservations of all locations on a given date")
    public Map<String, Integer> countReservedSeatsOfLocation(@PathVariable("date") String date){
        return iLocationDao.getCountOfReservations(CustomDate.parseString(date));
    }

    // the mail addresses are passed as url search params
    @PostMapping("/sendMails")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyAuthority('ADMIN','EMPLOYEE')")
    @ApiOperation(value = "Send mails (for absence) to all given students")
    public void sendEmailsAndAddPenaltyPoints(@RequestParam("mails") String[] mails) {
        // send mails
        emailService.sendMessages(applicationBundle.getString("absentStudents.mail.subject"),
                applicationBundle.getString("absentStudents.mail.template"), mails);
    }

    @PostMapping("/addPenaltyPoints/{location}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Assign penalty points to all given absent students")
    public void setReservationsToUnattendedAndAddPenaltyPoints(@RequestParam("ids") String[] augentIds, @PathVariable("location") String location) {
        LocalDate localDate = LocalDate.now();
        CustomDate customDate = new CustomDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0, 0);

        for (String augentId : augentIds) {
            LocationReservation locationReservation = iLocationReservationDao.getLocationReservation(augentId, customDate);
            if (locationReservation.getLocation().getName().toLowerCase().equals(location.toLowerCase())) {
                iLocationReservationDao.setReservationToUnAttended(augentId, customDate);
                int points = penaltyEventsDao.getPenaltyEvent(PenaltyEvent.CODE_NO_SHOWUP).getPoints();
                Penalty p = new Penalty(augentId, PenaltyEvent.CODE_NO_SHOWUP, customDate, locationReservation.getDate(), locationReservation.getLocation().getName(), points);
                penaltyEventsDao.addPenalty(p);
            }
        }
    }

    @ExceptionHandler(NoUserLoggedInWithGivenSessionIdMappingException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleUnauthorized() {
        System.out.println("some error");
    }

    @ExceptionHandler({IllegalArgumentException.class, Exception.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handle(Exception e) {
        System.out.println("some error");
    }
}
