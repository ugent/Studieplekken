package blok2.http.controllers;

import blok2.database.dao.ITimeslotDao;
import blok2.http.security.authorization.AuthorizedLocationController;
import blok2.exceptions.InvalidRequestParametersException;
import blok2.exceptions.NoSuchDatabaseObjectException;
import blok2.model.calendar.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("locations/timeslots")
public class TimeslotController extends AuthorizedLocationController {

    private final ITimeslotDao timeslotDAO;

    @Autowired
    public TimeslotController(ITimeslotDao calendarPeriodDao) {
        this.timeslotDAO = calendarPeriodDao;
    }

    @GetMapping("/details/{timeslotId}")
    @PreAuthorize("permitAll()")
    public Timeslot getTimeslot(@PathVariable("timeslotId") int timeslotId) {
        Timeslot timeslot = timeslotDAO.getTimeslot(timeslotId);
        if (timeslot == null)
            throw new NoSuchDatabaseObjectException("Timeslot does not exist");

        return timeslot;
    }

    @GetMapping("/{locationId}")
    @PreAuthorize("permitAll()")
    public List<Timeslot> getTimeslotsOfLocation(@PathVariable("locationId") int locationId) {
        // Only return timeslots from up to 6 months old until all in the future, to limit data sent to the frontend.
        return timeslotDAO.getTimeslotsOfLocationAfterTimeslotDate(locationId, LocalDate.now().minusMonths(6));
    }

    @PutMapping()
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public ResponseEntity<Object> updateTimeslot(@Valid @RequestBody Timeslot timeslot) {
        Timeslot original = timeslotDAO.getTimeslot(timeslot.getTimeslotSeqnr());
        // Validate original location
        isAuthorized(original.getLocationId());
        // Validate future location
        isAuthorized(timeslot.getLocationId());

        // Check that new number of seats is not less than number of reservations
        if (timeslot.getSeatCount() < original.getAmountOfReservations()) {
            return new ResponseEntity<>(new InvalidRequestParametersException("Number of seats cannot be less than number of reservations"), BAD_REQUEST);
        }

        // Execute update
        timeslotDAO.updateTimeslot(timeslot);

        return ResponseEntity.ok(timeslot);
    }


    @DeleteMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deleteTimeslot(@RequestBody Timeslot timeslot) {
        isAuthorized(timeslot.getLocationId());

        timeslotDAO.deleteTimeslot(timeslot);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public Timeslot addTimeslot(@RequestBody Timeslot timeslot) {
        isAuthorized(timeslot.getLocationId());

        return timeslotDAO.addTimeslot(timeslot);
    }

    @PutMapping("/{timeslotId}/repeatable")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public Timeslot setRepeatable(@PathVariable("timeslotId") Integer timeslotSequenceNumber, @RequestBody SetRepeatableBodyDao rep) {
        Timeslot ts = timeslotDAO.getTimeslot(timeslotSequenceNumber);
        isAuthorized(ts.getLocationId());
        ts.setRepeatable(rep.repeatable);
        return timeslotDAO.updateTimeslot(ts);
    }

    public static class SetRepeatableBodyDao {
        public boolean repeatable;
    }
}