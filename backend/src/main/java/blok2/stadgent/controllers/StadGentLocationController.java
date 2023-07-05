package blok2.stadgent.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ITimeslotDao;
import blok2.daos.services.LocationService;
import blok2.daos.services.TimeslotService;
import blok2.stadgent.model.StadGentLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("stadgent")
public class StadGentLocationController {

    private final ITimeslotDao timeslotService;
    private final ILocationDao locationService;

    @Autowired
    public StadGentLocationController(Environment env, ITimeslotDao timeslotService, ILocationDao locationService) {
        StadGentLocation.baseUrl = env.getProperty("custom.stadgent.url");

        this.timeslotService = timeslotService;
        this.locationService = locationService;
    }

    @GetMapping("/locations")
    public List<StadGentLocation> getLocations() {
        return this.locationService.getAllActiveLocations().stream().map(
                // Map each Location to a StadGentLocation.
                location -> StadGentLocation.fromLocation(location, timeslotService, locationService)
        ).sorted((location1, location2) -> {
            if (location1 == null || location2 == null) {
                return 0;
            }

            // Compare the availability information of the locations.
            if (location1.getHours().isEmpty() != location2.getHours().isEmpty()) {
                return location1.getHours().isEmpty() ? 1 : -1;
            }

            // Compare the amount of free seats.
            int freeSeats1 = location1.getCapacity() - location1.getReserved();
            int freeSeats2 = location2.getCapacity() - location2.getReserved();

            return freeSeats2 - freeSeats1;
        }).collect(Collectors.toList());
    }
}
