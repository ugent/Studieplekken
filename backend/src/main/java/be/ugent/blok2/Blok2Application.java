package be.ugent.blok2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@ServletComponentScan
@SpringBootApplication
public class Blok2Application extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(Blok2Application.class, args);
    }
}
