package com.tick_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tick_tracker.model.Sighting;

import java.util.List;

// Extending JpaRepository gives us save, findAll, count, etc. without writing them
public interface SightingRepository extends JpaRepository<Sighting, String> {
}

