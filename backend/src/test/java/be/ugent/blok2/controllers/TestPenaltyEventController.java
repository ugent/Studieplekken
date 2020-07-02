package be.ugent.blok2.controllers;

import be.ugent.blok2.configuration.RestAPITestAdapter;
import be.ugent.blok2.configuration.SecurityConfig;
import be.ugent.blok2.helpers.Language;
import be.ugent.blok2.model.penalty.PenaltyEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/*
 * This test file checks if the REST service handling the different penaltyEvents operations works properly
 * ! For some of the tests the DummyPenaltyEventsDao is needed
 * */
@SpringBootTest(classes = SecurityConfig.class)
@ActiveProfiles({"dummy","test"})
@AutoConfigureMockMvc
public class TestPenaltyEventController {
    private static final String BASE_URL = "/api/penalties";
    private RestAPITestAdapter restAPITestAdapter;

    private Map<Language, String> desc = new HashMap<>();
    private PenaltyEvent TEST_PENALTY_EVENT;
    private int nonExistingCode = 9999999;

    @Autowired
    public TestPenaltyEventController(MockMvc mockMvc) {
        restAPITestAdapter = new RestAPITestAdapter(mockMvc);
        desc.put(Language.ENGLISH, "Cancelling too late");
        TEST_PENALTY_EVENT = new PenaltyEvent(505, 505, true, desc);
    }

    /**
     *  BASE_URL should return a not empty array of PenaltyEvents.
     */
    @Test
    public void testGetPenaltyEvents() throws Exception {
        PenaltyEvent[] penaltyEvents = restAPITestAdapter.getOk(BASE_URL, PenaltyEvent[].class);
        assertNotNull(penaltyEvents);
        assertNotEquals(penaltyEvents.length, 0);
    }


    @Test
    public void testGetPenaltyEvent() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);

        PenaltyEvent penaltyEvent = restAPITestAdapter.getOk(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode(), PenaltyEvent.class);
        assertNotNull(penaltyEvent);
        assertEquals(penaltyEvent, TEST_PENALTY_EVENT);

        restAPITestAdapter.deleteNoContent(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode());
    }

    /**
     * Requesting a non existing PenaltyEvent should return 404 not found.
     */
    @Test
    public void testGetNonExistingPenaltyEvent() throws Exception {
        restAPITestAdapter.getNotFound(BASE_URL + '/' + nonExistingCode);
    }

    /**
     * adds a new penaltyEvent, requests it and deletes it
     * deleting it is needed so the other tests succeed, because they use the same penaltyEvent to test with
     */
    @Test
    public void testAddPenaltyEvent() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);

        PenaltyEvent addedPenaltyEvent = restAPITestAdapter.getOk(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode(), PenaltyEvent.class);
        assertNotNull(addedPenaltyEvent);
        assertEquals(TEST_PENALTY_EVENT, addedPenaltyEvent);

        restAPITestAdapter.deleteNoContent(BASE_URL + '/' + addedPenaltyEvent.getCode());
    }

    /**
     * Trying to add a penaltyEvent to a url with a different code in it should return a Bad Request status.
     */
    @Test
    public void testAddConflictingPenaltyEvent() throws Exception {
        restAPITestAdapter.postBadRequest(BASE_URL + '/' + nonExistingCode, TEST_PENALTY_EVENT);
    }

    /**
     * Trying to add a penalty event which has a code that's already in the db should return a Bad Request status.
     */
    @Test
    public void testAddExistingPenaltyEvent() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);
        restAPITestAdapter.postBadRequest(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode(), TEST_PENALTY_EVENT);
        restAPITestAdapter.deleteNoContent(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode());
    }

    /**
     * First the test adds a penaltyEvent, which already has one description in English,
     * then a description in Dutch is added,
     * the added penaltyEvent gets requested again and the 'asserts' check whether the description was added properly.
     * Finally the added event gets deleted.
     */
    @Test
    public void testAddDescription() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);
        String description = "beschrijving";

        restAPITestAdapter.postCreated(BASE_URL + "/description?code=" + TEST_PENALTY_EVENT.getCode() + "&language=" + Language.DUTCH + "&description=" + description, null );
        PenaltyEvent addedPenaltyEvent = restAPITestAdapter.getOk(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode(), PenaltyEvent.class);

        assertEquals(addedPenaltyEvent.getDescriptions().size(), 2);
        assertTrue(addedPenaltyEvent.getDescriptions().containsKey(Language.DUTCH));
        assertEquals(addedPenaltyEvent.getDescriptions().get(Language.DUTCH), description);

        restAPITestAdapter.deleteNoContent(BASE_URL + '/' + addedPenaltyEvent.getCode());
    }

    /**
     * Trying to add a description to a non existing penaltyEvent should return a bad request status
     */
    @Test
    public void testAddDescriptionToNonExisting() throws Exception {
        String description = "beschrijving";
        restAPITestAdapter.postBadRequest(BASE_URL + "/description?code=" + nonExistingCode + "&language=" + Language.DUTCH + "&description=" + description, null );
    }

    /**
     * Trying to add a description when it already exists, should return a bad request status.
     * (This should be done with a put/update request)
     */
    @Test
    public void testAddDescriptionAlreadyExistingLanguage() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);

        String description = "beschrijving";
        restAPITestAdapter.postBadRequest(BASE_URL + "/description?code=" + TEST_PENALTY_EVENT.getCode() + "&language=" + Language.ENGLISH + "&description=" + description, null );

        restAPITestAdapter.deleteNoContent(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode());
    }

    /**
     * Put request on a existing event should succeed
     */
    @Test
    public void testUpdatePenaltyEvent() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);
        restAPITestAdapter.put(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode(), TEST_PENALTY_EVENT);
        restAPITestAdapter.deleteNoContent(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode());
    }

    /**
     * When the code in the url and the code from the event in the body aren't equal, a Bad Request status should be returnd
     */
    @Test
    public void testUpdateConflictingPenaltyEvent() throws Exception {
        restAPITestAdapter.putBadRequest(BASE_URL + '/' + nonExistingCode, TEST_PENALTY_EVENT);
    }

    /**
     * Trying to update a non existing penaltyEvent should return a 404 not found status.
     */
    @Test
    public void testUpdateNonExistingPenaltyEvent() throws Exception {
        restAPITestAdapter.putNotFound(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode(), TEST_PENALTY_EVENT);
    }

    /**
     * A penaltyEvent is added and then deleted, trying to get is after the delete should return 404 not found.
     */
    @Test
    public void testDeletePenaltyEvent() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);

        restAPITestAdapter.deleteNoContent(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode());
        restAPITestAdapter.getNotFound(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode());
    }

    /**
     * Trying to delete a non existing penaltyEvent should return 404.
     */
    @Test
    public void testDeleteNonExistingPenaltyEvent() throws Exception {
        restAPITestAdapter.deleteNotFound(BASE_URL + '/' + nonExistingCode);
    }

    /*
        Adds penalty event, adds a description to it and then tries to delete it
     */
    @Test
    public void testDeleteDescription() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);
        String description = "beschrijving";

        restAPITestAdapter.postCreated(BASE_URL + "/description?code=" + TEST_PENALTY_EVENT.getCode() + "&language=" + Language.DUTCH + "&description=" + description, null );
        restAPITestAdapter.deleteNoContent(BASE_URL + "/description?code=" + TEST_PENALTY_EVENT.getCode() + "&language=" + Language.DUTCH );

        PenaltyEvent p = restAPITestAdapter.getOk(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode(), PenaltyEvent.class);
        assertEquals(p.getDescriptions().size(), 1);
        assertFalse(p.getDescriptions().containsKey(Language.DUTCH));

        restAPITestAdapter.deleteNoContent(BASE_URL+ '/' + p.getCode());
    }

    @Test
    public void testDeleteDescriptionNonExistingPenaltyEvent() throws Exception {
        restAPITestAdapter.deleteNotFound(BASE_URL + "/description?code=" + TEST_PENALTY_EVENT.getCode() + "&language=" + Language.DUTCH );
    }

    @Test
    public void testDeleteDescriptionNonExistingLanguage() throws Exception {
        restAPITestAdapter.postCreated(BASE_URL + '/' + TEST_PENALTY_EVENT.getCode() , TEST_PENALTY_EVENT);
        restAPITestAdapter.deleteNotFound(BASE_URL + "/description?code=" + TEST_PENALTY_EVENT.getCode() + "&language=" + Language.DUTCH );
        restAPITestAdapter.deleteNoContent(BASE_URL+ '/' + TEST_PENALTY_EVENT.getCode());
    }
}
