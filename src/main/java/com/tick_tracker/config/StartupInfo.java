package com.tick_tracker.config;

import com.tick_tracker.client.TickApiClient;
import com.tick_tracker.repository.SightingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupInfo {
    private final SightingRepository sightingRepository;
    private static final Logger log = LoggerFactory.getLogger(StartupInfo.class);
    private final TickApiClient tickApiClient;

    @Value("${server.port}")
    private int port;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;


    public StartupInfo(SightingRepository sightingRepository, TickApiClient tickApiClient) {
        this.sightingRepository = sightingRepository;
        this.tickApiClient = tickApiClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printLinks() {
        String base = "http://localhost:" + port;
        log.info("=========================================================");
        log.info(" Tick Tracker is running");
        log.info("   Health:     {}/health", base);
        log.info("   H2 console: {}/h2-console  (JDBC URL: {}, user: sa)", base, jdbcUrl);
        log.info(" Sightings in database: {}", sightingRepository.count());
        log.info("   Fetched from Elanco API: {}", tickApiClient.fetchAllSightings().size());
        log.info("=========================================================");

    }
}
