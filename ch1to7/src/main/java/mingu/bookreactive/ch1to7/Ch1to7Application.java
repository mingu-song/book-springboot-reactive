package mingu.bookreactive.ch1to7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thymeleaf.TemplateEngine;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class Ch1to7Application {

    public static void main(String[] args) {
        BlockHound.builder()
                .allowBlockingCallsInside(TemplateEngine.class.getCanonicalName(), "process")
                .install();

        SpringApplication.run(Ch1to7Application.class, args);
    }

}
