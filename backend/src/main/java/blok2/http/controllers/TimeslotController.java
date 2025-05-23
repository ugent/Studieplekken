package blok2.http.controllers;

import blok2.database.dao.IActionLogDao;
import blok2.database.dao.ITimeslotDao;
import blok2.exception.InvalidRequestParametersException;
import blok2.exception.NoSuchDatabaseObjectException;
import blok2.http.controllers.authorization.AuthorizedLocationController;
import blok2.model.ActionLogEntry;
import blok2.model.ActionLogEntry.Domain;
import blok2.model.ActionLogEntry.Type;
import blok2.model.calendar.Timeslot;
import blok2.model.users.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("locations/timeslots")
public class TimeslotController extends AuthorizedLocationController {
    private final ITimeslotDao timeslotDAO;
    private final IActionLogDao actionLogDao;

    @Autowired
    public TimeslotController(ITimeslotDao calendarPeriodDao, IActionLogDao actionLogDao) {
        this.timeslotDAO = calendarPeriodDao;
        this.actionLogDao = actionLogDao;
    }

    @GetMapping("/details/{timeslotId}")
    @PreAuthorize("permitAll()")
    public Timeslot getTimeslot(@PathVariable("timeslotId") int timeslotId) {
        Timeslot timeslot = timeslotDAO.getTimeslot(timeslotId);

        if (timeslot == null){
            throw new NoSuchDatabaseObjectException("Timeslot does not exist");
        }

        return timeslot;
    }

    @GetMapping("/{locationId}")
    @PreAuthorize("permitAll()")
    public List<Timeslot> getTimeslotsOfLocation(@PathVariable("locationId") int locationId) {
        // Only return timeslots from up to 6 months old until all in the future, to limit data sent to the frontend.
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return timeslotDAO.getTimeslotsOfLocationAfterTimeslotDate(locationId, sixMonthsAgo);
    }

    @PutMapping()
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public ResponseEntity<Object> updateTimeslot(@Valid @RequestBody Timeslot timeslot, @AuthenticationPrincipal User user) {
        // Authorize user for current and next locations.
        Timeslot original = timeslotDAO.getTimeslot(timeslot.getTimeslotSeqnr());
        this.checkLocationAuthorization(original.getLocationId());
        this.checkLocationAuthorization(timeslot.getLocationId());

        // Check that new number of seats is not less than number of reservations
        if (timeslot.getSeatCount() < original.getAmountOfReservations()) {
            throw new InvalidRequestParametersException(
                "Number of seats cannot be less than number of reservations"
            );
        }

        // Update the timeslot.
        this.timeslotDAO.updateTimeslot(timeslot);

        // Log the action.
        this.actionLogDao.addLogEntry(
            new ActionLogEntry(Type.UPDATE, user, Domain.LOCATION)
        );

        return ResponseEntity.ok(timeslot);
    }


    @DeleteMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deleteTimeslot(@RequestBody Timeslot timeslot) {
        checkLocationAuthorization(timeslot.getLocationId());

        timeslotDAO.deleteTimeslot(timeslot);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public Timeslot addTimeslot(@RequestBody Timeslot timeslot) {
        checkLocationAuthorization(timeslot.getLocationId());

        return timeslotDAO.addTimeslot(timeslot);
    }

    @PutMapping("/{timeslotId}/repeatable")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public Timeslot setRepeatable(@PathVariable("timeslotId") Integer timeslotSequenceNumber, @RequestBody SetRepeatableBodyDao rep) {
        Timeslot ts = timeslotDAO.getTimeslot(timeslotSequenceNumber);
        checkLocationAuthorization(ts.getLocationId());
        ts.setRepeatable(rep.repeatable);
        return timeslotDAO.updateTimeslot(ts);
    }

    public static class SetRepeatableBodyDao {
        public boolean repeatable;
    }
}