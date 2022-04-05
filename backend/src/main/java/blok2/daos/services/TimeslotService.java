package blok2.daos.services;

import blok2.daos.ITimeslotDao;
import blok2.daos.repositories.TimeslotRepository;
import blok2.helpers.exceptions.InvalidRequestParametersException;
import blok2.model.calendar.Timeslot;
import blok2.model.reservables.Location;
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
    public Timeslot getTimeslot(int timeslotSeqNr) {
        return timeslotRepository.getByTimeslotSeqnr(timeslotSeqNr);
    }

    @Override
    public List<Timeslot> addTimeslots(List<Timeslot> timeslot) {
        for (Timeslot t : timeslot) {
            Location loc = locationService.getLocationById(t.getLocationId());
            if (t.isReservable() && t.getReservableFrom() == null) {
                throw new InvalidRequestParametersException("Reservable timeslot is invalid.");
            }
            t.setSeatCount(loc.getNumberOfSeats());
            if (t.getTimeslotGroup() == null) {
                t.setTimeslotGroup(UUID.randomUUID());
            }

        }
        return timeslotRepository.saveAll(timeslot);
    }

    @Override
    public Timeslot addTimeslot(Timeslot timeslot) {
        Location loc = locationService.getLocationById(timeslot.getLocationId());
        timeslot.setSeatCount(loc.getNumberOfSeats());
        if (timeslot.getTimeslotGroup() == null) {
            timeslot.setTimeslotGroup(UUID.randomUUID());
        }
        return timeslotRepository.save(timeslot);
    }

    @Override
    public void deleteTimeslot(Timeslot timeslot) {
        timeslotRepository.deleteTimeslotByTimeslotSeqnr(timeslot.getTimeslotSeqnr());
    }

    @Override
    public Timeslot updateTimeslot(Timeslot timeslot) {
        Timeslot original = timeslotRepository.getByTimeslotSeqnr(timeslot.getTimeslotSeqnr());

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
