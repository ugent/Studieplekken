package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ITimeslotDAO;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.model.calendar.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("locations/timeslots")
public class TimeslotController extends  AuthorizedLocationController {

    private final Logger logger = Logger.getLogger(TimeslotController.class.getSimpleName());

    private final ITimeslotDAO timeslotDAO;
    private final ILocationDao locationDao;

    @Autowired
    public TimeslotController(ITimeslotDAO calendarPeriodDao,
                                    ILocationDao locationDao) {
        this.timeslotDAO = calendarPeriodDao;
        this.locationDao = locationDao;
    }

    @GetMapping("/{locationId}")
    @PreAuthorize("permitAll()")
    public List<Timeslot> getTimeslotsOfLocation(@PathVariable("locationId") int locationId) {
        return timeslotDAO.getTimeslotsOfLocation(locationId);
    }

    @PutMapping()
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void updateTimeslot(@Valid @RequestBody  Timeslot timeslot) {
        Timeslot original = timeslotDAO.getTimeslot(timeslot.getTimeslotSeqnr());
        // Validate original location
        isAuthorized(original.getLocationId());
        // Validate future location
        isAuthorized(timeslot.getLocationId());

        // Execute update
        timeslotDAO.updateTimeslot(timeslot);
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
}
