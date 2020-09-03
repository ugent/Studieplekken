package blok2.daos;

import blok2.helpers.Language;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;

import java.sql.SQLException;
import java.util.List;

public interface IPenaltyEventsDao extends IDao {

    /**
     * Get a list of all provided PenaltyEvents
     */
    List<PenaltyEvent> getPenaltyEvents() throws SQLException;

    /**
     * Get the PenaltyEvent associated with the given code.
     */
    PenaltyEvent getPenaltyEvent(int code) throws SQLException;

    /**
     * addPenaltyEvent() adds an entry in the data structure which holds the PenaltyEvents.
     * The code which identifies the PenaltyEvent is received through the object itself (event.getCode()).
     */
    void addPenaltyEvent(PenaltyEvent event) throws SQLException;

    /**
     * updatePenaltyEvent() is meant to update an existing PenaltyEvent and/or description(s) associated with the given code.
     * If there is no PenaltyEvent associated with the given code in the first place, nothing should happen.
     */
    void updatePenaltyEvent(int code, PenaltyEvent event) throws SQLException;

    /**
     * deletePenaltyEvent() deletes an existing PenaltyEvent.
     * When a PenaltyEvent gets deleted, it should invoke a cascade of deletions for the corresponding descriptions.
     */
    void deletePenaltyEvent(int code) throws SQLException;

    /**
     * addDescription() adds a description associated with the code.
     * If, in the data layer, there is no code found equal to the parameter, an entry should be created.
     * The same counts for the language: if there is no language associated with the given code, an entry should be created.
     */
    void addDescription(int code, Language language, String description) throws SQLException;

    /**
     * deleteDescription() deletes a description for given PenaltyEvent in given language
     */
    void deleteDescription(int code, Language language) throws SQLException;

    /**
     * Get the actual occurrence of a PenaltyEvent: e.g. someone has cancelled after 17:00 the day before opening
     */
    List<Penalty> getPenaltiesByUser(String augentId) throws SQLException;

    /**
     * Get all occurrences of a PenaltyEvent within a location
     */
    List<Penalty> getPenaltiesByLocation(String locationName) throws SQLException;

    /**
     * Get all occurrences of a PenaltyEvent by type of PenaltyEvent
     */
    List<Penalty> getPenaltiesByEventCode(int eventCode) throws SQLException;

    /**
     * addPenalty() adds a Penalty to the so called Penalty Book (like the Order Book in a stock exchange market)
     */
    void addPenalty(Penalty penalty) throws SQLException;

    /**
     * updatePenalty() removes the Penalties 'remove' and adds the Penalties 'add' for the user identified by augentID
     */
    void updatePenalties(String augentID, List<Penalty> remove, List<Penalty> add) throws SQLException;

    /**
     * deleteEvent() deletes a Penalty
     */
    void deletePenalty(Penalty penalty) throws SQLException;

}
