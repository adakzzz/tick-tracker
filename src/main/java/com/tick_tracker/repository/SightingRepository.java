package com.tick_tracker.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tick_tracker.model.Sighting;

// Extending JpaRepository gives us save, findAll, count, etc. without writing them
public interface SightingRepository extends JpaRepository<Sighting, String> {

    // Every filter is optional: when a parameter is null, its line matches every row
    @Query("""
            SELECT s FROM Sighting s
            WHERE (:location IS NULL OR LOWER(s.location) = LOWER(:location))
              AND (:from IS NULL OR s.dateTime >= :from)
              AND (:to IS NULL OR s.dateTime < :to)
            ORDER BY s.dateTime DESC
            """)
    List<Sighting> search(String location, LocalDateTime from, LocalDateTime to);
}