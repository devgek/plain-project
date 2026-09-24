package com.kah.plainproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@SpringBootApplication
@EnableJdbcAuditing
public class PlainProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlainProjectApplication.class, args);
    }
}
