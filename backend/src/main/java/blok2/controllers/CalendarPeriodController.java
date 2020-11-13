package blok2.controllers;

import blok2.daos.ICalendarPeriodDao;
import blok2.daos.ILocationDao;
import blok2.helpers.LocationStatus;
import blok2.helpers.Pair;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import blok2.shared.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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

    @PostMapping
    public void addCalendarPeriods(@RequestBody List<CalendarPeriod> calendarPeriods) {
        try {
            calendarPeriodDao.addCalendarPeriods(calendarPeriods);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{locationName}")
    public void updateCalendarPeriods(@PathVariable("locationName") String locationName,
                                      @RequestBody List<CalendarPeriod>[] fromAndTo) {
        try {
            List<CalendarPeriod> from = fromAndTo[0];
            List<CalendarPeriod> to = fromAndTo[1];

            // check for outdated view (perhaps some other user has changed the calendar periods in the meantime
            // between querying for the calendar periods for a location, and updating the calendar
            List<CalendarPeriod> currentView = calendarPeriodDao.getCalendarPeriodsOfLocation(locationName);

            // if the sizes dont match, the view must be different...
            if (from.size() != currentView.size()) {
                logger.log(Level.SEVERE, "updateCalendarPeriods, conflict in frontends data view and actual data view");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Wrong/Old view on data layer");
            }

            // if the sizes do match, check if the lists are equal
            // before invoking the 'equals' on a list, sort both lists based on 'starts at'
            Utility.sortPeriodsBasedOnStartsAt(currentView);
            Utility.sortPeriodsBasedOnStartsAt(from);

            if (!currentView.equals(from)) {
                logger.log(Level.SEVERE, "updateCalendarPeriods, conflict in frontends data view and actual data view");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Wrong/Old view on data layer");
            }

            // if the 'to' list is empty, all 'from' entries need to be deleted
            if (to.isEmpty()) {
                deleteCalendarPeriods(from);
            }
            // if the 'from' list is empty, all 'to' entries need to be added
            else if (from.isEmpty()) {
                addCalendarPeriods(to);
            } else {
                Utility.sortPeriodsBasedOnStartsAt(to);
                analyzeAndUpdateCalendarPeriods(locationName, from, to);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        } catch (ParseException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Wrong date format for 'starts at'");
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
    private void analyzeAndUpdateCalendarPeriods(String locationName,
                                                 List<CalendarPeriod> from,
                                                 List<CalendarPeriod> to) throws SQLException, ParseException {
        // setup
        Location expectedLocation = locationDao.getLocation(locationName);

        // analyze the periods
        for (CalendarPeriod period : to) {
            // all locations must match the expected location
            if (!expectedLocation.equals(period.getLocation())) {
                logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriods, conflict in locations");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Different locations in request");
            }

            // by parsing, we automatically check the string formats
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = format.parse(period.getStartsAt());
            Date endDate = format.parse(period.getEndsAt());

            // check if the ends of all periods are after the start
            if (endDate.getTime() < startDate.getTime()) {
                logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriods, endsAt was before startsAt");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "StartsAt must be before EndsAt");
            }

            // check if closingTime is not before the openingTime
            // this is done by using the same date, but different times
            format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            startDate = format.parse(period.getStartsAt() + " " + period.getOpeningTime());
            endDate = format.parse(period.getStartsAt() + " " + period.getClosingTime());

            if (endDate.getTime() < startDate.getTime()) {
                logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriods, closingTime was before openingTime");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "OpeningTime must be before closingTime");
            }

            // check if reservable from is parsable
            format.parse(period.getReservableFrom());
        }

        // delete all 'from' and add 'to'
        deleteCalendarPeriods(from);
        addCalendarPeriods(to);
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
}
