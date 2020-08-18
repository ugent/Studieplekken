package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.ICalendarPeriodForLockersDao;
import be.ugent.blok2.model.calendar.CalendarPeriodForLockers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/locations/lockerCalendar")
public class CalendarPeriodsForLockersController {

    private final Logger logger = Logger.getLogger(CalendarPeriodsForLockersController.class.getSimpleName());

    private final ICalendarPeriodForLockersDao calendarPeriodForLockersDao;

    @Autowired
    public CalendarPeriodsForLockersController(ICalendarPeriodForLockersDao calendarPeriodForLockersDao) {
        this.calendarPeriodForLockersDao = calendarPeriodForLockersDao;
    }

    @GetMapping("/{locationName}")
    public List<CalendarPeriodForLockers>
    getCalendarPeriodsForLockersOfLocation(@PathVariable("locationName") String locationName) {
        try {
            return this.calendarPeriodForLockersDao.getCalendarPeriodsForLockersOfLocation(locationName);
        } catch (SQLException ignore) {
            return null;
        }
    }

    @PostMapping
    public void addCalendarPeriodsForLockers(@RequestBody List<CalendarPeriodForLockers> calendarPeriodForLockers)
            throws SQLException {
        calendarPeriodForLockersDao.addCalendarPeriodsForLockers(calendarPeriodForLockers);
    }

    @PutMapping
    public void updateCalendarPeriodsForLockers(@RequestBody List<CalendarPeriodForLockers>[] fromAndTo) {
        List<CalendarPeriodForLockers> from = fromAndTo[0];
        List<CalendarPeriodForLockers> to = fromAndTo[1];

        logger.info("updateCalendarPeriodsForLockers, from size = " + from.size() + " and to size = " + to.size());
        // TODO: analyze the differences between from and to. Based on the analysis, call
        //   - calendarPeriodForLockersDao.addCalendarPeriodsForLockers(...)
        //   - calendarPeriodForLockersDao.updateCalendarPeriodsForLockers(..., ...)
        //   - calendarPeriodForLockersDao.deleteCalendarPeriodsForLockers(...)
    }

    @DeleteMapping
    public void deleteCalendarPeriodsForLockers(@RequestBody List<CalendarPeriodForLockers> calendarPeriodForLockers) {
        try {
            calendarPeriodForLockersDao.deleteCalendarPeriodsForLockers(calendarPeriodForLockers);
        } catch (SQLException ignore) {
            // Ignore
        }
    }
}
