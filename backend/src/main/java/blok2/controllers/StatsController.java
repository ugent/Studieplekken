package blok2.controllers;

import blok2.daos.ILocationDao;
import blok2.daos.ILocationReservationDao;
import blok2.daos.ITimeslotDao;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import blok2.model.reservations.LocationReservation;
import blok2.model.stats.InstitutionOverviewStat;
import blok2.model.stats.LocationOverviewStat;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("stats")
public class StatsController {

    private final ILocationDao locationDao;
    private final ITimeslotDao timeslotDAO;
    private final ILocationReservationDao locationReservationDao;

    @Autowired
    public StatsController(ILocationDao locationDao, ITimeslotDao timeslotDAO, ILocationReservationDao locationReservationDao) {
        this.locationDao = locationDao;
        this.timeslotDAO = timeslotDAO;
        this.locationReservationDao = locationReservationDao;
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

    @GetMapping("/locations/{locationId}/from/{from}/to/{to}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public LocationOverviewStat getStatsForLocation(@PathVariable("locationId") int locationId, @PathVariable("from") String stringDateFrom, @PathVariable("to") String stringDateTo) {
        LocalDate dateFrom = LocalDate.parse(stringDateFrom);
        LocalDate dateTo = LocalDate.parse(stringDateTo);

        Location location = locationDao.getLocationById(locationId);

        List<LocationReservation> locationReservations = locationReservationDao.getAllLocationReservationsOfLocationFromTo(locationId, dateFrom, dateTo);

        long reservationsTotal = locationReservations.stream()
                .filter(locationReservation -> locationReservation.getStateE().equals(LocationReservation.State.APPROVED)
                        || locationReservation.getStateE().equals(LocationReservation.State.PRESENT))
                .count();

        Map<String, Long> reservationsTotalPerHOI = locationReservations.stream()
                .filter(locationReservation -> locationReservation.getStateE().equals(LocationReservation.State.APPROVED)
                        || locationReservation.getStateE().equals(LocationReservation.State.PRESENT))
                .map(locationReservation -> locationReservation.getUser().getInstitution())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Long> reservationsPerDay = locationReservations.stream()
                .filter(locationReservation -> locationReservation.getStateE().equals(LocationReservation.State.APPROVED)
                        || locationReservation.getStateE().equals(LocationReservation.State.PRESENT))
                .map(locationReservation -> locationReservation.getTimeslot().timeslotDate().toString())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, Map<String, Long>> reservationsPerDayPerHOI = locationReservations.stream()
                .filter(locationReservation -> locationReservation.getStateE().equals(LocationReservation.State.APPROVED)
                        || locationReservation.getStateE().equals(LocationReservation.State.PRESENT))
                .collect(Collectors.groupingBy(locationReservation -> locationReservation.getTimeslot().timeslotDate().toString(),
                        Collectors.groupingBy(locationReservation -> locationReservation.getUser().getInstitution(), Collectors.counting())));

        return new LocationOverviewStat(locationId, location.getName(), reservationsTotal, reservationsTotalPerHOI, reservationsPerDay, reservationsPerDayPerHOI);
    }

    @GetMapping("/institutions/{institutionLocations}/students/{institutionStudents}/from/{from}/to/{to}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public InstitutionOverviewStat getStatsForInstitution(@PathVariable("institutionLocations") String institutionLocations, @PathVariable("institutionStudents") String institutionStudents, @PathVariable("from") String stringDateFrom, @PathVariable("to") String stringDateTo) {
        LocalDate dateFrom = LocalDate.parse(stringDateFrom);
        LocalDate dateTo = LocalDate.parse(stringDateTo);

        List<Location> locations = locationDao.getAllActiveLocations();
        List<LocationReservation> locationReservations = locations.stream().flatMap(location -> locationReservationDao.getAllLocationReservationsOfLocationFromTo(location.getLocationId(), dateFrom, dateTo).stream()).collect(Collectors.toList());
        // Calculate entries per institution based on full list above to reduce database calls
        List<Location> locationsForInstitution = institutionLocations.equalsIgnoreCase("ALL") ? locations : locations.stream().filter(location -> location.getInstitution().equals(institutionLocations)).collect(Collectors.toList());
        List<LocationReservation> locationReservationsForInstitution = institutionLocations.equalsIgnoreCase("ALL") ? locationReservations : locationsForInstitution.stream().flatMap(location -> locationReservations.stream().filter(locationReservation -> locationReservation.getTimeslot().getLocationId() == location.getLocationId())).collect(Collectors.toList());

        // Outgoing students = students from institutionStudents at each HOI
        Map<String, Long> outgoingStudentsPerHOI = locationReservations.stream()
                .filter(locationReservation -> locationReservation.getStateE().equals(LocationReservation.State.APPROVED)
                        || locationReservation.getStateE().equals(LocationReservation.State.PRESENT))
                .filter(locationReservation -> institutionStudents.equalsIgnoreCase("ALL") || locationReservation.getUser().getInstitution().equals(institutionStudents))
                .map(locationReservation -> locations.stream().filter(location -> location.getLocationId() == locationReservation.getTimeslot().getLocationId()).findFirst().map(Location::getInstitution).orElse("UNKNOWN"))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Incoming students = students from each HOI at institutionLocations
        Map<String, Long> incomingStudentsPerHOI = locationReservationsForInstitution.stream()
                .filter(locationReservation -> locationReservation.getStateE().equals(LocationReservation.State.APPROVED)
                        || locationReservation.getStateE().equals(LocationReservation.State.PRESENT))
                .map(locationReservation -> locationReservation.getUser().getInstitution())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Total reservations per day in institutionLocations from institutionStudents
        Map<String, Long> reservationsPerDay = locationReservationsForInstitution.stream()
                .filter(locationReservation -> locationReservation.getStateE().equals(LocationReservation.State.APPROVED)
                        || locationReservation.getStateE().equals(LocationReservation.State.PRESENT))
                .filter(locationReservation -> institutionStudents.equalsIgnoreCase("ALL") || locationReservation.getUser().getInstitution().equals(institutionStudents))
                .map(locationReservation -> locationReservation.getTimeslot().timeslotDate().toString())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return new InstitutionOverviewStat(institutionLocations, outgoingStudentsPerHOI, incomingStudentsPerHOI, reservationsPerDay);
    }
}
