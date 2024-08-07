package com.gemini.devsidekick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevSidekickApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevSidekickApplication.class, args);
    }

}
