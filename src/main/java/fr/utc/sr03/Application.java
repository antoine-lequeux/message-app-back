package fr.utc.sr03;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "fr.utc.sr03")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
