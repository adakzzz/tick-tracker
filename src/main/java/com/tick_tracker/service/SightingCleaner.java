package com.tick_tracker.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tick_tracker.client.SightingApiDto;
import com.tick_tracker.model.Sighting;

@Component
public class SightingCleaner {

    private static final Logger log = LoggerFactory.getLogger(SightingCleaner.class);

    // Turns raw API records into Sightings, skipping any that are incomplete or have a bad date
    public List<Sighting> clean(List<SightingApiDto> records) {
        List<Sighting> sightings = new ArrayList<>();
        int skippedIncomplete = 0;
        int skippedBadDate = 0;

        for (SightingApiDto record : records) {
            if (isBlank(record.id()) || isBlank(record.date()) || isBlank(record.location()) || isBlank(record.species()) || isBlank(record.latinName())) {
                skippedIncomplete++;
                continue;
            }

            LocalDateTime dateTime;
            try {
                dateTime = LocalDateTime.parse(record.date().trim());
            } catch (DateTimeParseException e) {
                skippedBadDate++;
                continue;
            }

            sightings.add(new Sighting(record.id().trim(), record.location().trim(), dateTime, record.species().trim(), record.latinName().trim()));
        }

        log.info("Cleaning: {} records in, {} kept, {} skipped as incomplete, {} skipped for a bad date",
                records.size(), sightings.size(), skippedIncomplete, skippedBadDate);
        return sightings;
    }

    // True if the value is missing (null) or only spaces
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
