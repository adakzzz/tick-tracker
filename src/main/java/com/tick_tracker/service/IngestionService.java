package com.tick_tracker.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.tick_tracker.client.SightingApiDto;
import com.tick_tracker.client.TickApiClient;
import com.tick_tracker.client.TickApiException;
import com.tick_tracker.model.Sighting;
import com.tick_tracker.repository.SightingRepository;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final TickApiClient tickApiClient;
    private final SightingCleaner sightingCleaner;
    private final SightingRepository sightingRepository;
    private final boolean ingestOnStartup;

    public IngestionService(TickApiClient tickApiClient, SightingCleaner sightingCleaner, SightingRepository sightingRepository, @Value("${tick.ingest.on-startup:true}") boolean ingestOnStartup) {
        this.tickApiClient = tickApiClient;
        this.sightingCleaner = sightingCleaner;
        this.sightingRepository = sightingRepository;
        this.ingestOnStartup = ingestOnStartup;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        if (!ingestOnStartup) {
            log.info("Startup ingestion is turned off");
            return;
        }
        ingest();
    }

    // API -> clean -> database
    public void ingest() {
        List<SightingApiDto> records;
        try {
            records = tickApiClient.fetchAllSightings();
        } catch (TickApiException e) {
            log.error("Could not load sightings from the Elanco API: {}. Running with {} sightings in the database.", e.getMessage(), sightingRepository.count());
            return;
        }

        List<Sighting> sightings = sightingCleaner.clean(records);
        sightingRepository.saveAll(sightings);
        log.info("Ingestion finished: fetched {} records, database now has {} sightings", records.size(), sightingRepository.count());
    }
}
