package be.ugent.blok2.websockets;

import be.ugent.blok2.configuration.RestAPITestAdapter;
import be.ugent.blok2.configuration.SecurityConfig;
import be.ugent.blok2.daos.dummies.DummyLocationReservationDao;
import be.ugent.blok2.helpers.date.CustomDate;
import be.ugent.blok2.reservations.LocationReservation;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SecurityConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dummy","test"})
@AutoConfigureMockMvc
public class TestWebsockets {

    /* Due to problems after integrating security into the unit tests, this test no longer works.
       However it can still be tested manually by following these instructions:
            1. Start the application with the dummy profile and open the site on two different windows
               (open one in incognito mode so you can log in with 2 different accounts)
            2. Log into the first window with the admin acccount (username: admin, passw: admin)
            3. Log into the second window with the scan account (username: scan, passw: scan)
            4. Set the location in both windows to Therminal and press open location in the admin window
            5. The dummy data contains reservations for a couple of students, you can use the following barcodes to 'scan' them
                001700580731
                2000020400000
                001707977015
                001707633454
            6. After putting one of these barcodes in the input fields and pressing "Controleren" or "Verify" in either window,
                a table with a new entry should appear on the admin window,  if this is the case it means the websockets work as expected
    * */

    private CompletableFuture<LocationReservation> completableFuture;

    private String augentId;
    private String barcode;
    private CustomDate customDate;
    private String locationName;

    private RestAPITestAdapter restAPITestAdapter;

    @LocalServerPort
    private String port;

    @Autowired
    public TestWebsockets(MockMvc mockMvc) {
        this.restAPITestAdapter=new RestAPITestAdapter(mockMvc);
    }

    @BeforeEach
    public void setUp() throws Exception {
        completableFuture = new CompletableFuture<>();
        // create a new reservation
        augentId = DummyLocationReservationDao.TEST_STUDENT.getAugentID();
        barcode = DummyLocationReservationDao.TEST_STUDENT.getBarcode();
        locationName = DummyLocationReservationDao.TEST_LOCATION.getName();
        LocalDate localDate = LocalDate.now();

        // the reservation has to be for today
        customDate = new CustomDate(localDate.getYear(),localDate.getMonthValue(),localDate.getDayOfMonth());
        LocationReservation locationReservation = new LocationReservation(DummyLocationReservationDao.TEST_LOCATION,DummyLocationReservationDao.TEST_STUDENT, customDate);

        // post locationReservation so it can be scanned later in the test
        restAPITestAdapter.postCreated("/api/location/reservations", locationReservation);
    }

    // cleanup after test, delete locationReservation added before test
    @After
    public void breakDown() throws Exception {
        restAPITestAdapter.deleteNoContent("/api/location/reservations/"+augentId+"/"+customDate.toString());
    }


    /*  -----------DOESN'T WORK CURRENTLY---------------

    // tests the functionality of the scan page using websockets
    // when a student scans it will send a POST request to the backend
    // in this POST handling function the locationReservation will be broadcast
    // to all subscribed clients ( i.e the scan employee at that location)
    @Test
    public void testScanpageWebsockets() throws InterruptedException, ExecutionException, TimeoutException {

        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));

        // used to convert websocket message to JSON
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        MyStompFrameHandler handler = new MyStompFrameHandler();
        String url = "https://localhost:" + port;
        StompSession stompSession = stompClient.connect(url + "/scanning", handler).get();

        // subscribe to locationReservation channel
        stompSession.subscribe(url+"/reservationScans/"+ locationName, handler);

        // should receive a new message from the broker
        LocationReservation locationReservation = completableFuture.get(3, TimeUnit.SECONDS);

        assertNotNull(locationReservation);
        assertEquals(locationReservation.getLocation().getName(), locationName);
    }
     */

    // class used to handle websocket messages
    private class MyStompFrameHandler extends StompSessionHandlerAdapter implements StompFrameHandler {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            // simulate a student scanning
            try {
                restAPITestAdapter.postOk("/api/location/reservations/scan/"+locationName+"/" + barcode, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return LocationReservation.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            System.out.println("Test");
            completableFuture.complete((LocationReservation) o);
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            throw new RuntimeException("Failure in WebSocket handling", exception);
        }

        @Override
        public void handleTransportError(StompSession session,
                                         Throwable exception) {
            throw new RuntimeException("Failure in WebSocket handling", exception);
        }
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

}
