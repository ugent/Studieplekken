package blok2.daos.services;

import blok2.daos.ITimeslotDAO;
import blok2.daos.repositories.TimeslotRepository;
import blok2.model.calendar.Timeslot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.threeten.extra.YearWeek;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class TimeslotService implements ITimeslotDAO {

    private final TimeslotRepository timeslotRepository;

    @Autowired
    public TimeslotService(TimeslotRepository repo) {
        this.timeslotRepository = repo;
    }

    @Override
    public List<Timeslot> getTimeslotsOfLocation(int locationId) {
        return this.timeslotRepository.getAllByLocationId(locationId);
    }

    @Override
    public Timeslot getTimeslot(int timeslotSeqNr) {
        return timeslotRepository.getByTimeslotSeqnr(timeslotSeqNr);
    }

    @Override
    public List<Timeslot> addTimeslots(List<Timeslot> timeslot) {
        return timeslotRepository.saveAll(timeslot);
    }

    @Override
    public Timeslot addTimeslot(Timeslot timeslot) {
        return timeslotRepository.save(timeslot);
    }

    @Override
    public void deleteTimeslot(Timeslot timeslot) {
        timeslotRepository.deleteTimeslotByTimeslotSeqnr(timeslot.getTimeslotSeqnr());
    }

    @Override
    public void updateTimeslot(Timeslot timeslot) {
        Timeslot original = timeslotRepository.getByTimeslotSeqnr(timeslot.getTimeslotSeqnr());

        original.setWeek(timeslot.getWeek());
        original.setOpeningHour(timeslot.getOpeningHour());
        original.setClosingHour(timeslot.getClosingHour());
        original.setSeatCount(timeslot.getSeatCount());
        original.setLocation(timeslot.getLocationId());
        original.setReservable(timeslot.isReservable());
        original.setReservableFrom(timeslot.getReservableFrom());

        timeslotRepository.save(original);
    }

    @Override
    public Optional<Timeslot> getCurrentOrNextTimeslot(int locationId) {
        LocalDateTime time = LocalDateTime.now();
        YearWeek week = YearWeek.from(time);
        return this.timeslotRepository.getCurrentOrNextTimeslot(locationId, week.getYear(), week.getWeek(), time.getDayOfWeek(), LocalTime.from(time), PageRequest.of(0,1)).stream().findFirst();
    }
}
