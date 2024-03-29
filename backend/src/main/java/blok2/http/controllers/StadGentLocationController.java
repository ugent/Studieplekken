package blok2.http.controllers;

import blok2.database.dao.ILocationDao;
import blok2.database.dao.ITimeslotDao;
import blok2.model.StadGentLocation;
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

    /**
     * Retrieve a list of active locations.
     *
     * @return a list of StadGentLocation objects representing the active locations.
     */
    @GetMapping("/locations")
    public List<StadGentLocation> getLocations() {
        return this.locationService.getAllActiveLocations().stream().map(location ->
                StadGentLocation.fromLocation(location, timeslotService, locationService)
        ).sorted(new RelevanceComparator()).collect(Collectors.toList());
    }

    /**
     * A relevance comparator for locations.
     */
    private static class RelevanceComparator implements Comparator<StadGentLocation> {
        @Override
        public int compare(StadGentLocation location1, StadGentLocation location2) {
            if (location1 == null || location2 == null) {
                return 0;
            }

            // Compare the availability information of the locations.
            if (location1.getHours().length() != location2.getHours().length()) {
                return location2.getHours().length() - location1.getHours().length();
            }

            // Compare the reservation ability information.
            if (location1.getStartDateReservation().length() != location2.getStartDateReservation().length()) {
                return location2.getStartDateReservation().length() - location1.getStartDateReservation().length();
            }

            // Compare the amount of free seats.
            int freeSeats1 = location1.getCapacity() - location1.getReserved();
            int freeSeats2 = location2.getCapacity() - location2.getReserved();

            return freeSeats2 - freeSeats1;
        }
    }
}
