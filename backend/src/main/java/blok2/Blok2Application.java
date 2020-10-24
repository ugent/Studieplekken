package blok2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.event.EventListener;
import org.springframework.web.context.support.RequestHandledEvent;

@ServletComponentScan
@SpringBootApplication
@EnableConfigurationProperties
public class Blok2Application extends SpringBootServletInitializer {

    private final Logger logger = LoggerFactory.getLogger(Blok2Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Blok2Application.class, args);
    }

    @EventListener
    public void requestListener(RequestHandledEvent event) {
        logger.info(String.format("Incoming request: %s", event.toString()));
    }
}
