package blok2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ConfigurationCheck {

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationCheck.class);

    @Autowired
    public ConfigurationCheck(Environment env) {
        String[] activeProfiles = env.getActiveProfiles();
        String[] recipientsOpeningHours = env
                .getProperty("custom.mailing.recipientsOpeningHoursOverview", String[].class);

        logger.info(String.format("Spring Boot started with spring.profiles.active = %s",
                Arrays.toString(activeProfiles)));
        logger.info(String.format("Spring Boot started with custom.mailing.recipientsOpeningHoursOverview = %s",
                Arrays.toString(recipientsOpeningHours)));
    }

}
