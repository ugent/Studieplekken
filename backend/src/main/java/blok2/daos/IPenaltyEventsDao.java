package blok2.daos;

import blok2.helpers.Language;
import blok2.model.penalty.Penalty;
import blok2.model.penalty.PenaltyEvent;

import java.sql.SQLException;
import java.util.List;

public interface IPenaltyEventsDao {

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

}
