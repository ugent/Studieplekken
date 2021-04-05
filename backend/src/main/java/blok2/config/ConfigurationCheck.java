package blok2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationCheck {

    private final static Logger logger = LoggerFactory.getLogger(ConfigurationCheck.class);

    @Autowired
    public ConfigurationCheck(Environment env) {
        String[] activeProfiles = env.getActiveProfiles();

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (String profile : activeProfiles)
            sb.append(String.format("'%s',", profile));
        sb.replace(sb.length()-1, sb.length(), "");
        sb.append(']');

        logger.info(String.format("Spring Boot started with spring.profiles.active = %s", sb.toString()));
    }

}
