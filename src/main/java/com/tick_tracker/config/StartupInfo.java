package com.tick_tracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupInfo {

    private static final Logger log = LoggerFactory.getLogger(StartupInfo.class);

    @Value("${server.port}")
    private int port;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void printLinks() {
        String base = "http://localhost:" + port;
        log.info("=========================================================");
        log.info(" Tick Tracker is running");
        log.info("   Local:      http://localhost:" + port);
        log.info("   Health:     {}/health", base);
        log.info("   H2 console: {}/h2-console  (JDBC URL: {}, user: sa)", base, jdbcUrl);
        log.info("=========================================================");
    }
}