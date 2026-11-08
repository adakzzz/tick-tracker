package com.tick_tracker.dto;

// One row of the "sightings per region" report
public record RegionCount(String region, long sightings) {
}