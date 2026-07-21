package com.example.orchardai;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.example.orchardai"})
public class OrchardAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrchardAiApplication.class, args);
    }
}
