package com.tick_tracker.controller;

import java.util.List;

import com.tick_tracker.dto.TrendPoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tick_tracker.dto.RegionCount;
import com.tick_tracker.service.StatsService;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // GET /stats/regions
    @GetMapping("/stats/regions")
    public List<RegionCount> sightingsPerRegion() {
        return statsService.sightingsPerRegion();
    }

    // GET /stats/trends?period=monthly (or weekly)
    @GetMapping("/stats/trends")
    public List<TrendPoint> trends(@RequestParam(defaultValue = "monthly") String period) {
        return statsService.trends(period);
    }
}
