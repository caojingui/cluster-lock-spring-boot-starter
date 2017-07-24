package com.tianba.starter.clusterlock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.tianba.starter.clusterlock.service.TestService;

@SpringBootApplication
public class StarterTestApplication {

    @Autowired
    private TestService testService;

    public static void main(String[] args) {
        SpringApplication.run(StarterTestApplication.class, args);
    }
}
