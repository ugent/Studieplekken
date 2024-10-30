package blok2.database.services;

import blok2.database.dao.ITimeslotDao;
import blok2.database.repositories.TimeslotRepository;
import blok2.exceptions.InvalidRequestParametersException;
import blok2.exceptions.NoSuchDatabaseObjectException;
import blok2.model.calendar.Timeslot;
import blok2.model.location.Location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimeslotService implements ITimeslotDao {

    private final TimeslotRepository timeslotRepository;
    private final LocationService locationService;

    @Autowired
    public TimeslotService(TimeslotRepository repo, LocationService locationService) {
        this.timeslotRepository = repo;
        this.locationService = locationService;
    }

    @Override
    public List<Timeslot> getTimeslotsOfLocation(int locationId) {
        return this.timeslotRepository.getAllByLocationId(locationId);
    }

    @Override
    public List<Timeslot> getTimeslotsOfLocationAfterTimeslotDate(int locationId, LocalDate timeslotDate) {
        return this.timeslotRepository.getAllByLocationIdAndAfterTimeslotDate(locationId, timeslotDate);
    }

    @Override
    public List<Timeslot> getTimeslotsOfLocationOnTimeslotDate(int locationId, LocalDate timeslotDate) {
        return this.timeslotRepository.getAllByLocationIdAndOnTimeslotDate(locationId, timeslotDate);
    }

    @Override
    public Timeslot getTimeslot(int timeslotSeqNr) {
        return timeslotRepository.getByTimeslotSeqnr(timeslotSeqNr);
    }

    @Override
    public List<Timeslot> addTimeslots(List<Timeslot> timeslots) {
        for (Timeslot timeslot : timeslots) {
            Location location = locationService.getLocationById(timeslot.getLocationId());

            if (timeslot.isReservable() && timeslot.getReservableFrom() == null) {
                throw new InvalidRequestParametersException("Reservable timeslot is invalid.");
            }

            timeslot.setSeatCount(location.getNumberOfSeats());

            if (location.getNumberOfSeats() > 50) {
                timeslot.setReservable(true);
            }

            if (timeslot.getTimeslotGroup() == null) {
                timeslot.setTimeslotGroup(UUID.randomUUID());
            }
        }

        return timeslotRepository.saveAll(timeslots);
    }

    @Override
    public Timeslot addTimeslot(Timeslot timeslot) {
        Location location = locationService.getLocationById(timeslot.getLocationId());

        if (location == null) {
            throw new NoSuchDatabaseObjectException("Location does not exist");
        }

        if (location.getNumberOfSeats() > 50) {
            timeslot.setReservable(true);
        }

        if (timeslot.getTimeslotGroup() == null) {
            timeslot.setTimeslotGroup(UUID.randomUUID());
        }

        timeslot.setSeatCount(location.getNumberOfSeats());

        return timeslotRepository.save(timeslot);
    }

    @Override
    public void deleteTimeslot(Timeslot timeslot) {
        timeslotRepository.deleteTimeslotByTimeslotSeqnr(timeslot.getTimeslotSeqnr());
    }

    @Override
    public Timeslot updateTimeslot(Timeslot timeslot) {
        Timeslot original = timeslotRepository.getByTimeslotSeqnr(timeslot.getTimeslotSeqnr());
        Location location = locationService.getLocationById(timeslot.getLocationId());

        if (original == null) throw new NoSuchDatabaseObjectException("Timeslot does not exist");
        if (location == null) throw new NoSuchDatabaseObjectException("Location does not exist");

        if (location.getNumberOfSeats() > 50) {
            timeslot.setReservable(true);
        }

        original.setTimeslotDate(timeslot.timeslotDate());
        original.setOpeningHour(timeslot.getOpeningHour());
        original.setClosingHour(timeslot.getClosingHour());
        original.setSeatCount(timeslot.getSeatCount());
        original.setLocation(timeslot.getLocationId());
        original.setReservable(timeslot.isReservable());
        original.setReservableFrom(timeslot.getReservableFrom());

        return timeslotRepository.save(original);
    }

    @Override
    public Optional<Timeslot> getCurrentOrNextTimeslot(int locationId) {
        LocalDateTime time = LocalDateTime.now();
        return this.timeslotRepository.getCurrentOrNextTimeslot(locationId, time.toLocalDate(), LocalTime.from(time), PageRequest.of(0, 1)).stream().findFirst();
    }
}
