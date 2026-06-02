package com.example.menedzeramatorskiejligisimracingowej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MALSRApplication {

    public static void main(String[] args) {
        SpringApplication.run(MALSRApplication.class, args);
    }

}