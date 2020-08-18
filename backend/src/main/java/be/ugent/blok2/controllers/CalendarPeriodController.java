package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.ICalendarPeriodDao;
import be.ugent.blok2.model.calendar.CalendarPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("api/locations/calendar")
public class CalendarPeriodController {

    private final Logger logger = Logger.getLogger(CalendarPeriodController.class.getSimpleName());

    private final ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    public CalendarPeriodController(ICalendarPeriodDao calendarPeriodDao) {
        this.calendarPeriodDao = calendarPeriodDao;
    }

    @GetMapping("/{locationName}")
    public List<CalendarPeriod> getCalendarPeriodsOfLocation(@PathVariable("locationName") String locationName) {
        try {
            return calendarPeriodDao.getCalendarPeriodsOfLocation(locationName);
        } catch (SQLException ignore) {
            return null;
        }
    }

    @PostMapping
    public void addCalendarPeriods(@RequestBody List<CalendarPeriod> calendarPeriods) throws SQLException {
        calendarPeriodDao.addCalendarPeriods(calendarPeriods);
    }

    @PutMapping
    public void updateCalendarPeriods(@RequestBody List<CalendarPeriod>[] fromAndTo) {
        List<CalendarPeriod> from = fromAndTo[0];
        List<CalendarPeriod> to = fromAndTo[1];

        logger.info("updateCalendarPeriods, from size = " + from.size() + " and to size = " + to.size());
        // TODO: analyze the differences between from and to. Based on the analysis, call
        //   - calendarPeriodDao.addCalendarPeriods(...)
        //   - calendarPeriodDao.updateCalendarPeriods(..., ...)
        //   - calendarPeriodDao.deleteCalendarPeriods(...)
    }

    @DeleteMapping
    public void deleteCalendarPeriods(@RequestBody List<CalendarPeriod> calendarPeriods) throws SQLException {
        calendarPeriodDao.deleteCalendarPeriods(calendarPeriods);
    }
}
