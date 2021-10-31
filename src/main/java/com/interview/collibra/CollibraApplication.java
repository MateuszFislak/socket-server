package com.interview.collibra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CollibraApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollibraApplication.class, args);
    }

}
