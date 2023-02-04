package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ITimeslotDao;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.stats.LocationStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("stats")
public class StatsController {

    private final ILocationDao locationDao;
    private final ITimeslotDao timeslotDAO;

    @Autowired
    public StatsController(ILocationDao locationDao, ITimeslotDao timeslotDAO) {
        this.locationDao = locationDao;
        this.timeslotDAO = timeslotDAO;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<LocationStat> getAllStats() {
        List<Location> locations = locationDao.getAllActiveLocations();
        locations.sort(Comparator.comparing(Location::getName));

        return locations.stream().map(location -> {
            Optional<Timeslot> currentOrNextTimeslot = timeslotDAO.getCurrentOrNextTimeslot(location.getLocationId());
            boolean open = false;
            boolean reservable = false;
            int numberOfSeats = 0;
            int numberOfTakenSeats = 0;
            LocalDateTime timeslotDate = null;

            if (currentOrNextTimeslot.isPresent()) {
                Timeslot timeslot = currentOrNextTimeslot.get();
                open = LocalDateTime.of(timeslot.timeslotDate(), timeslot.getOpeningHour()).isBefore(LocalDateTime.now());
                reservable = timeslot.isReservable();
                numberOfSeats = timeslot.getSeatCount();
                numberOfTakenSeats = timeslot.getAmountOfReservations();
                timeslotDate = LocalDateTime.of(timeslot.timeslotDate(), timeslot.getOpeningHour());
            }

            return new LocationStat(location.getLocationId(), location.getName(), open, reservable, numberOfSeats, numberOfTakenSeats, timeslotDate);
        }).collect(Collectors.toList());
    }

    @GetMapping("/{date}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<LocationStat> getAllStats(@PathVariable("date") String stringDate) {
        List<Location> locations = locationDao.getAllActiveLocations();
        locations.sort(Comparator.comparing(Location::getName));

        LocalDate date = LocalDate.parse(stringDate);

        return locations.stream().flatMap(location -> timeslotDAO.getTimeslotsOfLocationOnTimeslotDate(location.getLocationId(), date).stream().map(timeslot -> {
            boolean open = LocalDateTime.of(timeslot.timeslotDate(), timeslot.getOpeningHour()).isBefore(LocalDateTime.now());
            boolean reservable = timeslot.isReservable();
            int numberOfSeats = timeslot.getSeatCount();
            int numberOfTakenSeats = timeslot.getAmountOfReservations();
            LocalDateTime timeslotDate = LocalDateTime.of(timeslot.timeslotDate(), timeslot.getOpeningHour());

            return new LocationStat(location.getLocationId(), location.getName(), open, reservable, numberOfSeats, numberOfTakenSeats, timeslotDate);
        })).collect(Collectors.toList());
    }
}
