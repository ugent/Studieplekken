package blok2.database.dao;

import blok2.model.ActionLogEntry;

import java.util.List;

public interface IActionLogDao {

    /**
     * Add a new action to the action log.
     * @return : The added log entry.
     */
    ActionLogEntry addLogEntry(ActionLogEntry entry);

    /**
     * Get all actions logged in the database.
     * @return : all actions logged in the database.
     */
    List<ActionLogEntry> getAllActions();

}
