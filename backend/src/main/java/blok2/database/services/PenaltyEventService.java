package blok2.database.services;

import blok2.database.dao.IPenaltyEventsDao;
import blok2.database.repositories.PenaltyEventRepository;
import blok2.exceptions.NoSuchDatabaseObjectException;
import blok2.model.penalty.PenaltyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PenaltyEventService implements IPenaltyEventsDao {
    
    private final PenaltyEventRepository penaltyEventRepository;
    
    @Autowired
    public PenaltyEventService(PenaltyEventRepository penaltyEventRepository) {
        this.penaltyEventRepository = penaltyEventRepository;
    }
    
    @Override
    public List<PenaltyEvent> getPenaltyEvents() {
        return penaltyEventRepository.findAll();
    }

    @Override
    public PenaltyEvent getPenaltyEventByCode(int code) {
        return penaltyEventRepository.findById(code)
                .orElseThrow(() -> new NoSuchDatabaseObjectException(
                        String.format("No penalty event found with code '%d'", code)));
    }

    @Override
    public PenaltyEvent addPenaltyEvent(PenaltyEvent event) {
        return penaltyEventRepository.saveAndFlush(event);
    }

    @Override
    public void updatePenaltyEvent(PenaltyEvent event) {
        penaltyEventRepository.save(event);
    }

    @Override
    public void deletePenaltyEvent(int code) {
        penaltyEventRepository.deleteById(code);
    }
    
}
