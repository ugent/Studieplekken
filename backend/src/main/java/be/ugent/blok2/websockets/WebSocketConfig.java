package be.ugent.blok2.websockets;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    /*
     * This exposes a STOMP endpoint at URL /scanning
     * STOMP is a sub-protocol used by the WebSocket protocol, for more information see
     * https://docs.spring.io/spring-framework/docs/5.0.0.BUILD-SNAPSHOT/spring-framework-reference/html/websocket.html#websocket-stomp
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/scanning").withSockJS();
        /* Not all browsers support websockets or a company proxy server could block it, that's why SockJS is used as a fallback method
         * The goal of SockJS is to let applications use a WebSocket API but fall back to non-WebSocket alternatives when necessary at runtime,
         * i.e. without the need to change application code.
         */
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        /*
         * The app prefix is meant to differentiate between messages to be routed
         *  to message-handling methods to do application work vs messages
         *  to be routed to the broker to broadcast to subscribed clients.
         *
         * This project doesn't have any application work handling methods but it's still necessary to define this prefix.
         */
        config.enableSimpleBroker("/reservationScans");
        /*messages with destinations starting with these prefixes will be routed to the message broker,
         * where they will be broadcast to all subscribed clients.
         * In our case when a student scans his or her code at a location it wil trigger a post request
         * which will be handled by the LocationReservationController. This controller will send a message to
         * /reservationScans/{locationName} which will be broadcast to all subscribed clients (i.e. the scan employee of that location)
         */
    }
}
