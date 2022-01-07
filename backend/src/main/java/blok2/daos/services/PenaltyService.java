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
    private String EARLY_DELETE_CLASS = "profile.penalties.table.late.delete";
    private String NOT_ATTENDED_CLASS = "profile.penalties.table.not.attended";
    private int EARLY_DELETE_LIMIT = 4;
    private int EARLY_DELETE_COST = 20;
    private int NON_ATTENDANCE = 50;

    private final PenaltyRepository penaltyRepository;

    @Autowired
    public PenaltyService(PenaltyRepository penaltyRepository) {
        this.penaltyRepository = penaltyRepository;
    }

    @Override
    public List<Penalty> getPenaltiesByUser(String userId) {
        return penaltyRepository.findAllByDesignee(userId);
    }

    public List<Penalty> getAllPenalties() {
        return penaltyRepository.findAllByOrderByCreatedAtDesc();
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
        //int weeks = (int) ChronoUnit.WEEKS.between(p.getCreatedAt(), LocalDateTime.now());

        return p.getPoints();
    }

    public void notifyOfReservationDeletion(LocationReservation lr) {
        LocalDateTime opening = LocalDateTime.of(lr.getTimeslot().timeslotDate(), lr.getTimeslot().getOpeningHour());
        if(LocalDateTime.now().isAfter(opening.minus(1, ChronoUnit.DAYS))) {
            List<Penalty> penalties = this.getPenaltiesByUser(lr.getUser().getUserId());
            long amountOfEarlyDeletePenalties = penalties.stream()
                                                    .filter(p -> p.getPenaltyClass().equals(EARLY_DELETE_CLASS)).count();

            int points = amountOfEarlyDeletePenalties >= EARLY_DELETE_LIMIT ? EARLY_DELETE_COST : 0;

            Penalty penalty = new Penalty(points, "", null, EARLY_DELETE_CLASS, lr);
            this.addPenalty(penalty);
        }
    }

    public void notifyOfReservationAttendance(LocationReservation lr) {
        List<Penalty> l = penaltyRepository.findAllByLocationReservation(lr.getUser().getUserId(), lr.getTimeslot().getTimeslotSeqnr());
        if(lr.getStateE().equals(LocationReservation.State.ABSENT) && l.size() <= 0) {
            Penalty penalty = new Penalty(NON_ATTENDANCE, "", null, NOT_ATTENDED_CLASS, lr);
            this.addPenalty(penalty);
        }

        if(lr.getStateE().equals(LocationReservation.State.PRESENT) || lr.getStateE().equals(LocationReservation.State.PENDING)) {
            l.forEach(this::deletePenalty);
        }
    }
}
