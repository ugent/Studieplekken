package blok2.controllers;

import blok2.daos.ICalendarPeriodForLockersDao;
import blok2.daos.ILocationDao;
import blok2.model.calendar.CalendarPeriodForLockers;
import blok2.model.reservables.Location;
import blok2.shared.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping("api/locations/lockerCalendar")
public class CalendarPeriodsForLockersController {

    private final Logger logger = Logger.getLogger(CalendarPeriodsForLockersController.class.getSimpleName());

    private final ICalendarPeriodForLockersDao calendarPeriodForLockersDao;
    private final ILocationDao locationDao;

    @Autowired
    public CalendarPeriodsForLockersController(ICalendarPeriodForLockersDao calendarPeriodForLockersDao,
                                               ILocationDao locationDao) {
        this.calendarPeriodForLockersDao = calendarPeriodForLockersDao;
        this.locationDao = locationDao;
    }

    @GetMapping("/{locationName}")
    public List<CalendarPeriodForLockers>
    getCalendarPeriodsForLockersOfLocation(@PathVariable("locationName") String locationName) {
        try {
            return this.calendarPeriodForLockersDao.getCalendarPeriodsForLockersOfLocation(locationName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PostMapping
    public void addCalendarPeriodsForLockers(@RequestBody List<CalendarPeriodForLockers> calendarPeriodForLockers) {
        try {
            calendarPeriodForLockersDao.addCalendarPeriodsForLockers(calendarPeriodForLockers);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @PutMapping("/{locationName}")
    public void updateCalendarPeriodsForLockers(@PathVariable("locationName") String locationName,
                                                @RequestBody List<CalendarPeriodForLockers>[] fromAndTo) {
        try {
            List<CalendarPeriodForLockers> from = fromAndTo[0];
            List<CalendarPeriodForLockers> to = fromAndTo[1];

            // check for outdated view (perhaps some other user has changed the calendar periods in the meantime
            // between querying for the calendar periods for a location, and updating the calendar
            List<CalendarPeriodForLockers> currentView = calendarPeriodForLockersDao
                    .getCalendarPeriodsForLockersOfLocation(locationName);

            // if the sizes dont match, the view must be different...
            if (from.size() != currentView.size()) {
                logger.log(Level.SEVERE, "updateCalendarPeriodsForLockers, conflict in frontends data view and actual data view");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Wrong/Old view on data layer");
            }

            // if the sizes do match, check if the lists are equal before invoking
            // the 'equals' on a list, sort both lists based on 'starts at'
            Utility.sortPeriodsBasedOnStartsAt(currentView);
            Utility.sortPeriodsBasedOnStartsAt(from);

            if (!currentView.equals(from)) {
                logger.log(Level.SEVERE, "updateCalendarPeriodsForLockers, conflict in frontends data view and actual data view");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Wrong/Old view on data layer");
            }

            // if the 'to' list is empty, all 'from' entries need to be deleted
            if (to.isEmpty()) {
                deleteCalendarPeriodsForLockers(from);
            }
            // if the 'from' list is empty, all 'to' entries need to be added
            else if (from.isEmpty()) {
                addCalendarPeriodsForLockers(to);
            } else {
                Utility.sortPeriodsBasedOnStartsAt(to);
                analyzeAndUpdateCalendarPeriodsForLockers(locationName, from, to);
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
     * - no periods in 'to' may overlap
     * - all periods need to end after the start
     * <p>
     * Prerequisites:
     * - The parameter 'to' needs to be sorted based on 'startsAt' as date
     * (not the comparison of the string 'startsAt', but compare the date)
     * - 'to' is not empty
     * <p>
     * Special remarks:
     * - If the analysis has been done and all requisites are met, updating
     * means: deleting 'from' and adding 'to'. The reason is that it is too
     * costly to search for differences between 'from' and 'to'. Therefore,
     * the strategy of deleting the 'from' periods and adding new 'to' periods
     * was chosen
     */
    private void
    analyzeAndUpdateCalendarPeriodsForLockers(String locationName,
                                              List<CalendarPeriodForLockers> from,
                                              List<CalendarPeriodForLockers> to) throws SQLException, ParseException {
        // setup
        Location expectedLocation = locationDao.getLocation(locationName);
        Date lastEnd = null;

        // analyze the periods
        for (CalendarPeriodForLockers period : to) {
            // all locations must match the expected location
            if (!expectedLocation.equals(period.getLocation())) {
                logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriodsForLockers, conflict in locations");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Different locations in request");
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date startDate = format.parse(period.getStartsAt());
            Date endDate = format.parse(period.getEndsAt());

            // check if the ends of all periods are after the start
            if (endDate.getTime() < startDate.getTime()) {
                logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriodsForLockers, endsAt was before startsAt");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "StartsAt must be before EndsAt");
            }

            // make sure no periods overlap
            if (lastEnd != null && lastEnd.getTime() > startDate.getTime()) {
                logger.log(Level.SEVERE, "analyzeAndUpdateCalendarPeriodsForLockers, overlapping periods");
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Overlapping periods");
            }

            lastEnd = endDate;
        }

        // delete all 'from' and add 'to'
        deleteCalendarPeriodsForLockers(from);
        addCalendarPeriodsForLockers(to);
    }

    @DeleteMapping
    public void deleteCalendarPeriodsForLockers(@RequestBody List<CalendarPeriodForLockers> calendarPeriodForLockers) {
        try {
            calendarPeriodForLockersDao.deleteCalendarPeriodsForLockers(calendarPeriodForLockers);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
