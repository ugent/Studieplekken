package blok2.daos.services;

import blok2.daos.IPenaltyDao;
import blok2.daos.repositories.PenaltyRepository;
import blok2.model.penalty.Penalty;
import blok2.model.reservations.LocationReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PenaltyService implements IPenaltyDao {

    private int PENALTY_OFFSET = 20;

    private final PenaltyRepository penaltyRepository;

    @Autowired
    public PenaltyService(PenaltyRepository penaltyRepository) {
        this.penaltyRepository = penaltyRepository;
    }

    @Override
    public List<Penalty> getPenaltiesByUser(String userId) {
        return penaltyRepository.findAllByDesignee(userId);
    }

    @Override
    public Penalty addPenalty(Penalty penalty) {
        return penaltyRepository.saveAndFlush(penalty);
    }

    @Override
    public void deletePenalty(Penalty penalty) {
        penaltyRepository.deleteById(penalty.getPenaltyId());
    }

    @Override
    public int getUserPenalty(String userid) {
        List<Penalty> penaltyList = this.getPenaltiesByUser(userid);
        return penaltyList.stream().map(this::getActualPointsOfPenalty).reduce(0, Integer::sum);
    }

    private int getActualPointsOfPenalty(Penalty p) {
        int weeks = (int) ChronoUnit.WEEKS.between(p.getCreatedAt(), LocalDateTime.now());

        return Math.max(p.getPoints() - weeks * PENALTY_OFFSET, 0);
    }

    public void notifyOfReservationDeletion(LocationReservation lr) {
        LocalDateTime opening = LocalDateTime.of(lr.getTimeslot().timeslotDate(), lr.getTimeslot().getOpeningHour());
        if(LocalDateTime.now().isAfter(opening.minus(1, ChronoUnit.DAYS))) {
            Penalty penalty = new Penalty(20, "", null, "profile.penalties.table.late.delete", lr);
            this.addPenalty(penalty);
        }
    }

    public void notifyOfReservationAttendance(LocationReservation lr) {
        List<Penalty> l = penaltyRepository.findAllByLocationReservation(lr.getUser().getUserId(), lr.getTimeslot().getTimeslotSeqnr());
        if(lr.getStateE().equals(LocationReservation.State.ABSENT) && l.size() <= 0) {
            Penalty penalty = new Penalty(25, "", null, "profile.penalties.table.not.attended", lr);
            this.addPenalty(penalty);
        }

        if(lr.getStateE().equals(LocationReservation.State.PRESENT) || lr.getStateE().equals(LocationReservation.State.PENDING)) {
            l.forEach(this::deletePenalty);
        }
    }
}
