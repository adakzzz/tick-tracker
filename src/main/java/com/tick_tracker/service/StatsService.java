package com.tick_tracker.service;

import java.util.List;

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
}