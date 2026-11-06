package com.tick_tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Sighting {

    @Id
    private String id;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String species;

    @Column(nullable = false)
    private String latinName;

    protected Sighting() {
        // JPA needs this to create the object when loading a row
    }

    public Sighting(String id, String location, LocalDateTime dateTime, String species, String latinName) {
        this.id = id;
        this.location = location;
        this.dateTime = dateTime;
        this.species = species;
        this.latinName = latinName;


    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getSpecies() {
        return species;
    }

    public String getLatinName() {
        return latinName;
    }

}
