package blok2.daos.services;

import blok2.daos.ILocationDao;
import blok2.daos.repositories.LocationRepository;
import blok2.helpers.exceptions.NoSuchDatabaseObjectException;
import blok2.helpers.orm.LocationNameAndNextReservableFrom;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class LocationService implements ILocationDao {

    private final LocationRepository locationRepository;

    @Autowired
    @Lazy
    private TimeslotService timeslotService;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public Location getLocationById(int locationId) {
        Location l = locationRepository.findById(locationId)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("Location with locationId '%d' does not exist.", locationId)));
        initializeCurrentTimeslot(l);
        return l;
    }

    @Override
    public Location getLocationByName(String locationName) {
        Location l = locationRepository.findLocationByName(locationName)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("Location with locationName '%s' does not exist.", locationName)));
        initializeCurrentTimeslot(l);
        return l;
    }

    @Override
    public List<Location> getAllActiveLocations() {
        List<Location> locs = locationRepository.findAllActiveLocations();
        locs.forEach(this::initializeCurrentTimeslot);
        return locs;
    }

    @Override
    public List<Location> getAllUnapprovedLocations() {
        return locationRepository.findAllByApprovedFalse();
    }

    @Override
    public List<LocationNameAndNextReservableFrom> getNextReservationMomentsOfAllLocations() {
        return locationRepository.getNextReservationMomentsOfAllLocations();
    }

    @Override
    public Location addLocation(Location location) {
        location.setApproved(true);
        return locationRepository.saveAndFlush(location);
    }

    @Override
    public void updateLocation(Location location) {
        Location originalLocation = locationRepository.getOne(location.getLocationId());
        originalLocation.setBuilding(location.getBuilding());
        originalLocation.setName(location.getName());
        originalLocation.setAuthority(location.getAuthority());
        originalLocation.setNumberOfSeats(location.getNumberOfSeats());
        originalLocation.setForGroup(location.getForGroup());
        originalLocation.setImageUrl(location.getImageUrl());
        originalLocation.setDescriptionDutch(location.getDescriptionDutch());
        originalLocation.setDescriptionEnglish((location.getDescriptionEnglish()));
        originalLocation.setUsesPenaltyPoints(location.isUsesPenaltyPoints());
        locationRepository.save(originalLocation);
    }

    @Override
    public void deleteLocation(int locationId) {
        locationRepository.deleteById(locationId);
    }

    @Override
    public void approveLocation(Location location, boolean approval) {
        location.setApproved(approval);
        locationRepository.save(location);
    }

    @Override
    public Map<String, String[]> getOpeningOverviewOfWeek(int year, int isoWeek) {
        // The SQL query that will be used requires the dates of a monday and following sunday
        // of a week for which the opening hours will be calculated. However, the week number
        // (according to the ISO 8601 standard) in a year are given as parameters.
        // Therefore, the dates of the monday and sunday of the corresponding week need to be
        // calculated.
        //
        // This can be done using the with() method of LocalDate. This method gives an adjusted
        // copy of a LocalDate object. By adjusting the week number of a LocalDate object, followed
        // by another with() to adjust the week day to a "monday", we can obtain the date of
        // the monday corresponding to the week given by the parameter `weekNr`.
        //
        // This methodology needs a base LocalDate object that can be adjusted. This LocalDate
        // object's year must be determined by the 'year' parameter. The exact day of the year
        // is of no importance since it will be adjusted later to be the monday of the week
        // given by `weekNr`. However, the day of the year may not be before the first monday
        // of the year. Therefore, the 50th day of the week is chosen.
        //
        // source: https://stackoverflow.com/a/32186362/9356123
        LocalDate mondayDate = LocalDate.ofYearDay(year, 50)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, isoWeek)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sundayDate = mondayDate.plusDays(6);

        List<String[]> retval = locationRepository.getOpeningHoursOverview(mondayDate, sundayDate);

        // Use a TreeMap to order the keys, this results in a user friendly overview.
        Map<String, String[]> overview = new TreeMap<>();
        for (String[] row : retval) {
            overview.put(row[0], Arrays.copyOfRange(row, 1, row.length));
        }

        return overview;
    }

    private void initializeCurrentTimeslot(Location location) {
        location.setCurrentTimeslot(timeslotService.getCurrentOrNextTimeslot(location.getLocationId()).orElse(null));
    }

    public void initializeTags(Location location) {
        List<Timeslot> timeslotsOfLocation = this.timeslotService.getTimeslotsOfLocation(location.getLocationId());
        LocalDate date = LocalDate.now();

        location.setTomorrowStillAvailable(timeslotsOfLocation.stream().filter(timeslot -> timeslot.timeslotDate().isEqual(LocalDate.now().plusDays(1))).anyMatch(timeslot -> timeslot.getAmountOfReservations() < timeslot.getSeatCount()));
        location.setOpenDuringWeek(timeslotsOfLocation.stream().filter(timeslot -> timeslot.timeslotDate().isAfter(date) && timeslot.timeslotDate().isBefore(date.plusDays(8))).anyMatch(timeslot -> timeslot.timeslotDate().getDayOfWeek() != DayOfWeek.SATURDAY && timeslot.timeslotDate().getDayOfWeek() != DayOfWeek.SUNDAY));
        location.setOpenDuringWeekend(timeslotsOfLocation.stream().filter(timeslot -> timeslot.timeslotDate().isAfter(date) && timeslot.timeslotDate().isBefore(date.plusDays(8))).anyMatch(timeslot -> timeslot.timeslotDate().getDayOfWeek() == DayOfWeek.SATURDAY || timeslot.timeslotDate().getDayOfWeek() == DayOfWeek.SUNDAY));
        location.setOpenDuringEvening(timeslotsOfLocation.stream().filter(timeslot -> timeslot.timeslotDate().isAfter(date) && timeslot.timeslotDate().isBefore(date.plusDays(8))).anyMatch(timeslot -> !timeslot.getClosingHour().isBefore(LocalTime.of(20, 0))));
        location.setOptionalNextUpcomingReservableTimeslot(timeslotsOfLocation.stream().filter(timeslot -> timeslot.isReservable() && timeslot.getReservableFrom().isAfter(LocalDateTime.now())).min(Comparator.comparing(Timeslot::timeslotDate)));
    }
}
