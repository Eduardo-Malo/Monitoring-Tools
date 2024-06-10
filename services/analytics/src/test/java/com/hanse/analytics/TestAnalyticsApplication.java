package com.hanse.analytics;

import org.springframework.boot.SpringApplication;

public class TestAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.from(AnalyticsApplication::main)
                         .with(TestcontainersConfiguration.class)
                         .run(args);
    }

}
