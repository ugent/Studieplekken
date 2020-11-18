package blok2.controllers;

import blok2.daos.ICalendarPeriodDao;
import blok2.daos.ILocationDao;
import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.calendar.Period;
import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class CalendarPeriodController {

    private final Logger logger = Logger.getLogger(CalendarPeriodController.class.getSimpleName());

    private final ICalendarPeriodDao calendarPeriodDao;
    private final ILocationDao locationDao;

    @Autowired
    public CalendarPeriodController(ICalendarPeriodDao calendarPeriodDao,
                                    ILocationDao locationDao) {
        this.calendarPeriodDao = calendarPeriodDao;
        this.locationDao = locationDao;
    }

    @GetMapping("/{locationName}")
    public List<CalendarPeriod> getCalendarPeriodsOfLocation(@PathVariable("locationName") String locationName) {
        try {
            return calendarPeriodDao.getCalendarPeriodsOfLocation(locationName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping("/{locationName}/status")
    public Pair<LocationStatus, String> getStatusOfLocation(@PathVariable("locationName") String locationName) {
        try {
            return calendarPeriodDao.getStatus(locationName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @GetMapping
    public List<CalendarPeriod> getAllCalendarPeriods() {
        try {
            return calendarPeriodDao.getAllCalendarPeriods();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{locationName}")
    public void updateCalendarPeriods(@PathVariable("locationName") String locationName,
                                      @RequestBody UpdateCalendarBody fromAndTo) {
        try {
            List<CalendarPeriod> from = fromAndTo.getPrevious();
            CalendarPeriod to = fromAndTo.getToUpdate();

            List<CalendarPeriod> currentView = calendarPeriodDao.getCalendarPeriodsOfLocation(locationName);

            // if the sizes do match, check if the lists are equal
            // before invoking the 'equals' on a list, sort both lists based on 'starts at'
            currentView.sort(Comparator.comparing(Period::getStartsAt));
            from.sort(Comparator.comparing(Period::getStartsAt));

            if (!currentView.equals(from)) {
                logger.log(Level.SEVERE, "updateCalendarPeriods, conflict in frontends data view and actual data view");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Wrong/Old view on data layer");
            }

            to.initializeLockedFrom();
            analyzeUpdatedCalendarPeriod(locationName, to);

            // If this is a new CalendarPeriod, add instead
            if (to.getId() == null) {
                // TODO unless admin
                if (!to.isLocked())
                    calendarPeriodDao.addCalendarPeriods(Collections.singletonList(to));
                else {
                    logger.log(Level.SEVERE, "updateCalendarPeriods, new CalendarPeriod too late");
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT, "new calendarperiod too late.");
                }
                return;
            }

            // Note: if the if-clause above evaluated to true, the method addCalendarPeriods would
            // have set the id of the calendar period. So, this is safe.
            CalendarPeriod originalTo = calendarPeriodDao.getById(to.getId());

            // TODO unless admin
            if (originalTo.isLocked()) {
                logger.log(Level.SEVERE, "updateCalendarPeriods, already locked");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "The original term is locked.");
            }

            to.initializeLockedFrom();
            if (to.isLocked()) {
                logger.log(Level.SEVERE, "updateCalendarPeriods, move to locked space");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "The time you're moving into is already locked.");
            }

            calendarPeriodDao.updateCalendarPeriod(to);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    /**
     * Analyzing means:
     * - all periods to be updated need to be for the same location
     * - some checks on each period are performed:
     *     1. checking the formats of startsAt/endsAt, openingTime/closingTime and reservableFrom
     *     2. endsAt may not be before startsAt
     *     3. closingTime may not be before openingTime
     * <p>
     * Special remarks:
     * - If the analysis has been done and all requisites are met, updating
     * means: deleting 'from' and adding 'to'. The reason is that it is too
     * costly to search for differences between 'from' and 'to'. Therefore,
     * the strategy of deleting the 'from' periods and adding new 'to' periods
     * was chosen
     */
    private void analyzeUpdatedCalendarPeriod(String locationName,
                                              CalendarPeriod to) throws SQLException {
        // setup
        Location expectedLocation = locationDao.getLocation(locationName);

        // analyze the periods
        // all locations must match the expected location
        if (!expectedLocation.equals(to.getLocation())) {
            logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriods, conflict in locations");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Different locations in request");
        }


        // check if the ends of all periods are after the start
        if (to.getEndsAt().isBefore(to.getStartsAt())) {
            logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriods, endsAt was before startsAt");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "StartsAt must be before EndsAt");
        }


        if (to.getOpeningTime().isAfter(to.getClosingTime())) {
            logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriods, closingTime was before openingTime");
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "OpeningTime must be before closingTime");
        }


        // check if reservable from is parsable
        if (to.isReservable()) {
            if(to.getReservableTimeslotSize() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Timeslot size must be larger than 0.");
            }

            if(!to.getReservableFrom().isAfter(to.getLockedFrom())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "ReservableFrom must be after locked date. (3 weeks)");

            }
        }

    }

    @DeleteMapping
    public void deleteCalendarPeriods(@RequestBody List<CalendarPeriod> calendarPeriods) {
        try {
            calendarPeriodDao.deleteCalendarPeriods(calendarPeriods);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
    
    private static class UpdateCalendarBody {
        private List<CalendarPeriod> previous;
        private CalendarPeriod toUpdate;

        public List<CalendarPeriod> getPrevious() {
            return previous;
        }

        public void setPrevious(List<CalendarPeriod> previous) {
            this.previous = previous;
        }

        public CalendarPeriod getToUpdate() {
            return toUpdate;
        }

        public void setToUpdate(CalendarPeriod toUpdate) {
            this.toUpdate = toUpdate;
        }
    }
}
