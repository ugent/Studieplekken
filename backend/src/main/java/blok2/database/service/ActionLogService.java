package blok2.database.service;

import blok2.database.dao.IActionLogDao;
import blok2.database.repository.ActionLogRepository;
import blok2.model.ActionLogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActionLogService implements IActionLogDao {

    private final ActionLogRepository actionLogRepository;

    @Autowired
    public ActionLogService(ActionLogRepository actionLogRepository) {
        this.actionLogRepository = actionLogRepository;
    }

    @Override
    public ActionLogEntry addLogEntry(ActionLogEntry entry) {
        return actionLogRepository.save(entry);
    }

    @Override
    public List<ActionLogEntry> getAllActions() {
        return actionLogRepository.findAll();
    }
}
