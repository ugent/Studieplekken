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
     * // TODO(ydndonck): Should probably only return a subset right? Should see if it becomes a problem maybe.
     * @return : all actions logged in the database.
     */
    List<ActionLogEntry> getAllActions();

}
