package com.hugoserve.metalbroker.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    private final DataSource dataSource;

    @PostConstruct
    public void migrate() {

        System.out.println("========== FLYWAY CONFIG START ==========");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .load();

        flyway.migrate();

        System.out.println("========== FLYWAY CONFIG END ==========");
    }
}