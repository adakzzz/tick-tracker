package com.tick_tracker.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tick_tracker.model.Sighting;
import com.tick_tracker.repository.SightingRepository;

@Service
public class SightingService {

    private final SightingRepository sightingRepository;

    public SightingService(SightingRepository sightingRepository) {
        this.sightingRepository = sightingRepository;
    }

    // Turns the user's filters into a precise database query
    public List<Sighting> search(String location, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must be on or before 'to'");
        }
        String cleanLocation = (location == null || location.isBlank()) ? null : location.trim();
        LocalDateTime fromTime = (from == null) ? null : from.atStartOfDay();
        LocalDateTime toTime = (to == null) ? null : to.plusDays(1).atStartOfDay();
        return sightingRepository.search(cleanLocation, fromTime, toTime);
    }
}