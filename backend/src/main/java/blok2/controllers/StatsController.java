package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ITimeslotDao;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.stats.LocationStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
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
            int numberOfSeats = location.getNumberOfSeats();
            int numberOfTakenSeats = 0;

            if (currentOrNextTimeslot.isPresent()) {
                Timeslot timeslot = currentOrNextTimeslot.get();
                open = timeslot.getOpeningHour().isBefore(LocalTime.now());
                reservable = timeslot.isReservable();
                numberOfSeats = timeslot.getSeatCount();
                numberOfTakenSeats = timeslot.getAmountOfReservations();
            }

            return new LocationStat(location.getLocationId(), location.getName(), open, reservable, numberOfSeats, numberOfTakenSeats);
        }).collect(Collectors.toList());
    }
}
