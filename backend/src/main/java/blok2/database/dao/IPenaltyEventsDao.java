package blok2.database.dao;

import blok2.model.penalty.PenaltyEvent;

import java.util.List;

public interface IPenaltyEventsDao {

    /**
     * Get a list of all provided PenaltyEvents
     */
    List<PenaltyEvent> getPenaltyEvents();

    /**
     * Get the PenaltyEvent associated with the given code.
     */
    PenaltyEvent getPenaltyEventByCode(int code);

    /**
     * Adds a PenaltyEvent
     */
    PenaltyEvent addPenaltyEvent(PenaltyEvent event);

    /**
     * Update an existing PenaltyEvent
     */
    void updatePenaltyEvent(PenaltyEvent event);

    /**
     * Deletes an existing PenaltyEvent.
     */
    void deletePenaltyEvent(int code);

}
