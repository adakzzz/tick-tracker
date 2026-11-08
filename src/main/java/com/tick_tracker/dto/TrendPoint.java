package com.tick_tracker.dto;

// One point on the trend line: a week or month, and how many sightings it had
public record TrendPoint(String period, long sightings) {
}