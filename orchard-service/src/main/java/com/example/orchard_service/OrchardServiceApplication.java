package com.example.orchard_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example"})
public class OrchardServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchardServiceApplication.class, args);
    }

}
