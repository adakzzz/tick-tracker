package com.tick_tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.tick_tracker.dto.RegionCount;
import com.tick_tracker.dto.TrendPoint;
import com.tick_tracker.model.Sighting;
import com.tick_tracker.repository.SightingRepository;

@DataJpaTest
class StatsServiceTest {

    @Autowired
    private SightingRepository sightingRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        sightingRepository.saveAll(List.of(
                new Sighting("a1", "London", LocalDateTime.of(2022, 1, 10, 9, 0), "Marsh tick", "Ixodes apronophorus"),
                new Sighting("a2", "London", LocalDateTime.of(2022, 1, 16, 23, 30), "Marsh tick", "Ixodes apronophorus"),
                new Sighting("a3", "Leeds", LocalDateTime.of(2022, 3, 1, 12, 0), "Marsh tick", "Ixodes apronophorus")));
        statsService = new StatsService(sightingRepository);
    }

    @Test
    void countsSightingsPerRegionBusiestFirst() {
        List<RegionCount> result = statsService.sightingsPerRegion();

        assertEquals(List.of(new RegionCount("London", 2), new RegionCount("Leeds", 1)), result);
    }

    @Test
    void groupsByMonthOldestFirst() {
        List<TrendPoint> result = statsService.trends("monthly");

        assertEquals(List.of(new TrendPoint("2022-01", 2), new TrendPoint("2022-03", 1)), result);
    }

    @Test
    void groupsByWeekStartingMonday() {
        List<TrendPoint> result = statsService.trends("weekly");

        assertEquals(List.of(new TrendPoint("2022-01-10", 2), new TrendPoint("2022-02-28", 1)), result);
    }

    @Test
    void rejectsUnknownPeriod() {
        assertThrows(IllegalArgumentException.class, () -> statsService.trends("yearly"));
    }
}
