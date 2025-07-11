package com.crossborder.hospitalA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class SystemAApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemAApplication.class, args);
    }
}
