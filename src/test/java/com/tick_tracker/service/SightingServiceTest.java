package com.tick_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.tick_tracker.model.Sighting;
import com.tick_tracker.repository.SightingRepository;

@DataJpaTest
class SightingServiceTest {

    @Autowired
    private SightingRepository sightingRepository;

    private SightingService sightingService;

    @BeforeEach
    void setUp() {
        sightingRepository.saveAll(List.of(
                new Sighting("a1", "London", LocalDateTime.of(2022, 1, 10, 9, 0), "Marsh tick", "Ixodes apronophorus"),
                new Sighting("a2", "London", LocalDateTime.of(2022, 3, 31, 23, 30), "Marsh tick", "Ixodes apronophorus"),
                new Sighting("a3", "Leeds", LocalDateTime.of(2022, 6, 1, 12, 0), "Marsh tick", "Ixodes apronophorus")));
        sightingService = new SightingService(sightingRepository);
    }

    @Test
    void noFiltersReturnsEverythingNewestFirst() {
        List<Sighting> result = sightingService.search(null, null, null);

        assertEquals(3, result.size());
        assertEquals("a3", result.get(0).getId());
    }

    @Test
    void filtersByLocationIgnoringCase() {
        assertEquals(2, sightingService.search("london", null, null).size());
    }

    @Test
    void toDateIncludesTheWholeDay() {
        List<Sighting> result = sightingService.search(null, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 31));

        assertEquals(1, result.size());
        assertEquals("a2", result.get(0).getId());
    }

    @Test
    void combinesLocationAndDates() {
        assertEquals(1, sightingService.search("London", LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31)).size());
        assertEquals(0, sightingService.search("Leeds", LocalDate.of(2022, 1, 1), LocalDate.of(2022, 1, 31)).size());
    }
}