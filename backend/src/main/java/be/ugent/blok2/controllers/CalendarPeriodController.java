package be.ugent.blok2.controllers;

import be.ugent.blok2.daos.ICalendarPeriodDao;
import be.ugent.blok2.model.calendar.CalendarPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("api/locations/calendar")
public class CalendarPeriodController {
    private final ICalendarPeriodDao calendarPeriodDao;

    @Autowired
    public CalendarPeriodController(ICalendarPeriodDao calendarPeriodDao) {
        this.calendarPeriodDao = calendarPeriodDao;
    }

    @GetMapping("/{locationName}")
    public List<CalendarPeriod> getCalendarPeriodsOfLocation(@PathVariable("locationName") String locationName) {
        try {
            return this.calendarPeriodDao.getCalendarPeriodsOfLocation(locationName);
        } catch (SQLException ignore) {
            return null;
        }
    }

    @PutMapping
    public void updateCalendarPeriods(@RequestBody List<CalendarPeriod> from,
                                      @RequestBody List<CalendarPeriod> to) {
        // TODO: analyze the differences between from and to. Based on the analysis, call
        //   - calendarPeriodDao.addCalendarPeriods(...)
        //   - calendarPeriodDao.updateCalendarPeriods(..., ...)
        //   - calendarPeriodDao.deleteCalendarPeriods(...)
    }
}
