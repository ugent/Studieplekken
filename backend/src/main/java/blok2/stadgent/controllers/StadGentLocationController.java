package blok2.stadgent.controllers;

import blok2.daos.services.LocationService;
import blok2.daos.services.TimeslotService;
import blok2.stadgent.model.StadGentLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("stadgent")
public class StadGentLocationController {
    private final TimeslotService ts;

    @Autowired
    public StadGentLocationController(Environment env, TimeslotService ts) {
        StadGentLocation.baseUrl = env.getProperty("custom.stadgent.url");
        this.ts = ts;
    }

    @Autowired
    private LocationService locationService;

    @GetMapping("/locations")
    public List<StadGentLocation> getLocations() {
        return this.locationService.getAllActiveLocations().stream().map(t -> StadGentLocation.fromLocation(t, ts, locationService)).collect(Collectors.toList());
    }
}
