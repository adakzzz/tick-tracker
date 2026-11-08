package com.tick_tracker.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.tick_tracker.dto.TrendPoint;
import com.tick_tracker.model.Sighting;
import org.springframework.stereotype.Service;

import com.tick_tracker.dto.RegionCount;
import com.tick_tracker.repository.SightingRepository;

@Service
public class StatsService {

    private final SightingRepository sightingRepository;

    public StatsService(SightingRepository sightingRepository) {
        this.sightingRepository = sightingRepository;
    }

    public List<RegionCount> sightingsPerRegion() {
        return sightingRepository.countByRegion();
    }

    // Counts sightings per week or month, oldest first
    public List<TrendPoint> trends(String period) {
        if (!period.equals("weekly") && !period.equals("monthly")) {
            throw new IllegalArgumentException("period must be 'weekly' or 'monthly'");
        }

        Map<String, Long> counts = new TreeMap<>();
        for (Sighting sighting : sightingRepository.findAll()) {
            String key = periodOf(sighting.getDateTime(), period);
            counts.put(key, counts.getOrDefault(key, 0L) + 1);
        }

        List<TrendPoint> trend = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            trend.add(new TrendPoint(entry.getKey(), entry.getValue()));
        }
        return trend;
    }

    // "2022-08" for monthly, or the Monday that starts the week ("2022-08-01") for weekly
    private String periodOf(LocalDateTime dateTime, String period) {
        if (period.equals("monthly")) {
            return YearMonth.from(dateTime).toString();
        }
        return dateTime.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
    }
}
