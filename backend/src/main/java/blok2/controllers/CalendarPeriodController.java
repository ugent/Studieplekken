package blok2.controllers;

import blok2.daos.ICalendarPeriodDao;
import blok2.daos.ILocationDao;
import blok2.helpers.Pair;
import blok2.helpers.authorization.AuthorizedLocationController;
import blok2.helpers.LocationStatus;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Period;
import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("locations/calendar")
public class CalendarPeriodController extends  AuthorizedLocationController {

    private final Logger logger = Logger.getLogger(CalendarPeriodController.class.getSimpleName());

    private final ICalendarPeriodDao calendarPeriodDao;
    private final ILocationDao locationDao;

    @Autowired
    public CalendarPeriodController(ICalendarPeriodDao calendarPeriodDao,
                                    ILocationDao locationDao) {
        this.calendarPeriodDao = calendarPeriodDao;
        this.locationDao = locationDao;
    }

    @GetMapping("/{locationId}")
    @PreAuthorize("permitAll()")
    public List<CalendarPeriod> getCalendarPeriodsOfLocation(@PathVariable("locationId") int locationId) {
        try {
            return calendarPeriodDao.getCalendarPeriodsOfLocation(locationId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<CalendarPeriod> getAllCalendarPeriods() {
        try {
            return calendarPeriodDao.getAllCalendarPeriods();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{locationId}")
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void updateCalendarPeriods(@PathVariable("locationId") int locationId,
                                      @RequestBody CalendarPeriod calendarPeriod) {
        isAuthorized(locationId);

        try {
            // If this is a new CalendarPeriod, add instead
            if (calendarPeriod.getId() == null) {
                calendarPeriodDao.addCalendarPeriods(Collections.singletonList(calendarPeriod));
            }

            calendarPeriodDao.updateCalendarPeriod(calendarPeriod);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }


    @DeleteMapping
    @PreAuthorize("hasAuthority('HAS_AUTHORITIES') or hasAuthority('ADMIN')")
    public void deleteCalendarPeriods(@RequestBody CalendarPeriod calendarPeriod) {
        isAuthorized(calendarPeriod.getLocation().getLocationId());

        try {
            calendarPeriodDao.deleteCalendarPeriod(calendarPeriod);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
