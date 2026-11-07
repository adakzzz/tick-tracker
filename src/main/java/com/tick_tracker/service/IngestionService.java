package com.tick_tracker.service;

import com.tick_tracker.client.SightingApiDto;
import com.tick_tracker.client.TickApiClient;
import com.tick_tracker.model.Sighting;
import com.tick_tracker.repository.SightingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private final TickApiClient tickApiClient;
    private final SightingRepository sightingRepository;
    private final SightingCleaner sightingCleaner;

    public IngestionService(TickApiClient tickApiClient, SightingRepository sightingRepository, SightingCleaner sightingCleaner) {
        this.tickApiClient = tickApiClient;
        this.sightingRepository = sightingRepository;
        this.sightingCleaner = sightingCleaner;
    }

    //API -> convert -> database
    @EventListener(ApplicationReadyEvent.class)
    public void ready() {
        List< SightingApiDto> records = tickApiClient.fetchAllSightings();
        List<Sighting> sightings = sightingCleaner.clean(records);
        for (SightingApiDto record : records){
            sightings.add(toSighting(record));
        }

        sightingRepository.saveAll(sightings);
        log.info("Ingestion finished: fetched {} records, database now has {} sightings",
                records.size(), sightingRepository.count());
    }

    // Turns one API record into a database entity
    private Sighting toSighting(SightingApiDto record) {
        LocalDateTime dateTime = LocalDateTime.parse(record.date());
        return new Sighting(record.id(), record.location(), dateTime, record.species(), record.latinName());
    }
}

