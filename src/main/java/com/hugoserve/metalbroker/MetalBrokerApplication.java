package com.hugoserve.metalbroker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;


@EnableScheduling
@EnableCaching
@SpringBootApplication
public class MetalBrokerApplication {

    public static void main(String[] args) {

        SpringApplication.run(MetalBrokerApplication.class, args);
    }

}
