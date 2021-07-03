package blok2.daos.services;

import blok2.daos.IPenaltyDao;
import blok2.daos.repositories.PenaltyRepository;
import blok2.model.penalty.Penalty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PenaltyService implements IPenaltyDao {

    private final PenaltyRepository penaltyRepository;

    @Autowired
    public PenaltyService(PenaltyRepository penaltyRepository) {
        this.penaltyRepository = penaltyRepository;
    }

    @Override
    public List<Penalty> getPenaltiesByUser(String userId) {
        return penaltyRepository.findAllByUserId(userId);
    }

    @Override
    public List<Penalty> getPenaltiesByLocation(int locationId) {
        return penaltyRepository.findAllByLocationId(locationId);
    }

    @Override
    public List<Penalty> getPenaltiesByEventCode(int eventCode) {
        return penaltyRepository.findAllByPenaltyEventCode(eventCode);
    }

    @Override
    public Penalty addPenalty(Penalty penalty) {
        return penaltyRepository.saveAndFlush(penalty);
    }

    @Override
    public void deletePenalty(Penalty penalty) {
        penaltyRepository.deleteById(penalty.getPenaltyId());
    }

}
