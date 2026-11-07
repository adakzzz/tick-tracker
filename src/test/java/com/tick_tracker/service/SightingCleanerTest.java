package com.tick_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tick_tracker.client.SightingApiDto;
import com.tick_tracker.model.Sighting;

class SightingCleanerTest {

    private final SightingCleaner cleaner = new SightingCleaner();

    // A complete, correct record. Each test breaks one thing on purpose.
    private SightingApiDto validRecord(String id) {
        return new SightingApiDto(id, "2022-08-01T06:40:31", "London", "Marsh tick", "Ixodes apronophorus");
    }

    @Test
    void keepsAndConvertsValidRecord() {
        SightingApiDto withSpaces = new SightingApiDto(" a1 ", "2022-08-01T06:40:31", " London ", "Marsh tick", "Ixodes apronophorus");
        List<Sighting> result = cleaner.clean(List.of(withSpaces));
        assertEquals(1, result.size());
        Sighting sighting = result.get(0);
        assertEquals("a1", sighting.getId());
        assertEquals("London", sighting.getLocation());
        assertEquals(LocalDateTime.of(2022, 8, 1, 6, 40, 31), sighting.getDateTime());
    }

    @Test
    void skipsRecordWithMissingField() {
        SightingApiDto noLocation = new SightingApiDto("a2", "2022-08-01T06:40:31", null, "Marsh tick", "Ixodes apronophorus");
        List<Sighting> result = cleaner.clean(List.of(validRecord("a1"), noLocation));
        assertEquals(1, result.size());
        assertEquals("a1", result.get(0).getId());
    }

    @Test
    void skipsRecordWithBadDate() {
        SightingApiDto badDate = new SightingApiDto("a2", "not a date", "London", "Marsh tick", "Ixodes apronophorus");
        List<Sighting> result = cleaner.clean(List.of(validRecord("a1"), badDate));
        assertEquals(1, result.size());
        assertEquals("a1", result.get(0).getId());
    }

    @Test
    void skipsDuplicateIdAndKeepsFirst() {
        SightingApiDto first = new SightingApiDto("a1", "2022-08-01T06:40:31", "London", "Marsh tick", "Ixodes apronophorus");
        SightingApiDto second = new SightingApiDto("a1", "2022-08-01T06:40:31", "Leeds", "Marsh tick", "Ixodes apronophorus");
        List<Sighting> result = cleaner.clean(List.of(first, second));
        assertEquals(1, result.size());
        assertEquals("London", result.get(0).getLocation());
    }

    @Test
    void forgetsSeenIdsBetweenCalls() {
        cleaner.clean(List.of(validRecord("a1")));
        List<Sighting> secondRun = cleaner.clean(List.of(validRecord("a1")));
        assertEquals(1, secondRun.size());
    }
}